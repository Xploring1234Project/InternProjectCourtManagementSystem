package com.cognizant.JusticeServe.service.impl;

import com.cognizant.JusticeServe.dto.request.CaseRequest;
import com.cognizant.JusticeServe.dto.request.DocumentRequest;
import com.cognizant.JusticeServe.dto.request.NotificationRequest;
import com.cognizant.JusticeServe.dto.response.CaseResponse;
import com.cognizant.JusticeServe.dto.response.DocumentResponse;
import com.cognizant.JusticeServe.entity.*;
import com.cognizant.JusticeServe.exception.BadRequestException;
import com.cognizant.JusticeServe.exception.ResourceNotFoundException;
import com.cognizant.JusticeServe.repository.CaseDocumentRepository;
import com.cognizant.JusticeServe.repository.CaseRepository;
import com.cognizant.JusticeServe.repository.CitizenRepository;
import com.cognizant.JusticeServe.repository.UserRepository;
import com.cognizant.JusticeServe.service.AuditLogService;
import com.cognizant.JusticeServe.service.CaseService;
import com.cognizant.JusticeServe.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaseServiceImpl implements CaseService {

    private final CaseRepository caseRepo;
    private final CitizenRepository citizenRepo;
    private final UserRepository userRepo;
    private final CaseDocumentRepository docRepo;
    private final NotificationService notificationService;
    private final AuditLogService  auditLogService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public CaseResponse fileNewCase(CaseRequest request) {
        log.info("Filing case for citizenId={}, lawyerId={}", request.getCitizenId(), request.getLawyerId());
        Citizen citizen = citizenRepo.findById(request.getCitizenId())
                .orElseThrow(() -> new ResourceNotFoundException("Citizen not found: " + request.getCitizenId()));

        Case newCase = modelMapper.map(request, Case.class);
        newCase.setCitizen(citizen);
        newCase.setStatus(Case.CaseStatus.FILED);

        if (request.getLawyerId() != null) {
            User lawyer = userRepo.findById(request.getLawyerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lawyer not found: " + request.getLawyerId()));
            newCase.setLawyer(lawyer);
        }

        Case savedCase = caseRepo.save(newCase);
        log.info("Case #{} filed: '{}'", savedCase.getCaseId(), savedCase.getTitle());

        auditLogService.recordSystemAction(
                citizen.getUser().getUserId(),
                "CASE_FILED",
                "Case:" + savedCase.getCaseId() + " - " + savedCase.getTitle());

        if (savedCase.getLawyer() != null) {
            // Notify Citizen
            sendNotification(citizen.getUser().getUserId(), savedCase.getCaseId(),
                    "Your case \"" + savedCase.getTitle() + "\" (Case #" + savedCase.getCaseId() + ") has been filed successfully. " +
                            "Your lawyer " + savedCase.getLawyer().getUserId() + " has been linked. " +
                            "Please stay tuned for further hearing and process updates.");

            // Notify Lawyer if selected during filing
            sendNotification(savedCase.getLawyer().getUserId(), savedCase.getCaseId(),
                    "Citizen " + citizen.getName() + " has filed Case #" + savedCase.getCaseId() +
                            ": \"" + savedCase.getTitle() + "\" and selected you as their lawyer. " +
                            "Please review all documents, analyse the case, and stay tuned for further hearings.");
        }else{
            sendNotification(citizen.getUser().getUserId(), savedCase.getCaseId(),
                    "Your case \"" + savedCase.getTitle() + "\" (Case #" + savedCase.getCaseId() + ") has been filed successfully."+
                    "The court clerk will review and assign a lawyer soon.");
        }

        return modelMapper.map(savedCase, CaseResponse.class);
    }

    @Override
    public CaseResponse getCaseById(Long id) {
        Case c = caseRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found: " + id));
        return modelMapper.map(c, CaseResponse.class);
    }

    @Override
    public List<CaseResponse> getAllCases() {
        return caseRepo.findAll().stream()
                .map(c -> modelMapper.map(c, CaseResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CaseResponse> getCasesByCitizenId(Long citizenId) {
        return caseRepo.findByCitizenCitizenId(citizenId).stream()
                .map(c -> modelMapper.map(c, CaseResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CaseResponse> getCasesByLawyerId(Long lawyerId) {
        return caseRepo.findByLawyerUserId(lawyerId).stream()
                .map(c -> modelMapper.map(c, CaseResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CaseResponse> getCasesByStatus(Case.CaseStatus status) {
        return caseRepo.findByStatus(status).stream()
                .map(c -> modelMapper.map(c, CaseResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CaseResponse updateCaseStatus(Long id, Case.CaseStatus status) {
        log.info("Updating case #{} status to {}", id, status);

        Case c = caseRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found: " + id));
        Case.CaseStatus oldStatus = c.getStatus();

        if (c.getStatus() == Case.CaseStatus.CLOSED) {
            throw new BadRequestException("Cannot update status: Case is already CLOSED.");
        }

        c.setStatus(status);
        Case updatedCase = caseRepo.save(c);

        auditLogService.recordSystemAction(
                updatedCase.getCitizen().getUser().getUserId(),
                "CASE_STATUS_UPDATED",
                "Case:" + id + " [" + oldStatus + " -> " + status + "]"
        );

        sendNotification(updatedCase.getCitizen().getUser().getUserId(), id,
                "Your case \"" + updatedCase.getTitle() + "\" (Case #" + id + ") status has been updated to: " + status.name() + ".");

        return modelMapper.map(c, CaseResponse.class);
    }

    @Override
    @Transactional
    public CaseResponse assignLawyerToCase(Long caseId, Long lawyerId) {
        log.info("Assigning lawyerId={} to caseId={}", lawyerId, caseId);
        Case c = caseRepo.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found: " + caseId));
        User lawyer = userRepo.findById(lawyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Lawyer not found: " + lawyerId));

        if (c.getStatus() == Case.CaseStatus.CLOSED || c.getStatus() == Case.CaseStatus.JUDGEMENT_PENDING) {
            throw new BadRequestException("Cannot assign lawyer at this stage of the case.");
        }

        // Validation: Check if the user is actually a Lawyer
        if (lawyer.getRole() != User.Role.LAWYER) {
            throw new IllegalArgumentException("User with ID " + lawyerId + " is not a Lawyer.");
        }

        c.setLawyer(lawyer);
        Case savedCase = caseRepo.save(c);

        log.info("Lawyer {} assigned to Case #{}", lawyer.getName(), caseId);

        auditLogService.recordSystemAction(lawyer.getUserId(), "LAWYER_ASSIGNED",
                "Case:" + caseId + " - Lawyer: " + lawyer.getName());

        // Notify Lawyer of new assignment
        sendNotification(lawyer.getUserId(), caseId,
                "You have been assigned as the lawyer for Case #" + caseId + ": \"" + savedCase.getTitle() + "\". " +
                        "Citizen: " + savedCase.getCitizen().getName() + ". " +
                        "Please review all case documents, analyse the matter, and stay tuned for hearing schedules and further updates.");

        // Notify Citizen that a lawyer is now assigned
        sendNotification(savedCase.getCitizen().getUser().getUserId(), caseId,
                "Great news! Your case \"" + savedCase.getTitle() + "\" (Case #" + caseId + ") has been accepted by the court clerk. " +
                        "Your assigned lawyer is " + lawyer.getName() + ". " +
                        "Please stay tuned for further hearing and process updates.");

        return modelMapper.map(c, CaseResponse.class);
    }

    @Override
    @Transactional
    public CaseResponse removeLawyerFromCase(Long caseId) {
        log.info("Removing lawyer from caseId={}", caseId);

        Case c = caseRepo.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found: " + caseId));

        if (c.getStatus() == Case.CaseStatus.CLOSED) {
            throw new BadRequestException("Cannot remove lawyer: Case is already CLOSED.");
        }

        User removedLawyer = c.getLawyer();
        if (removedLawyer == null) {
            throw new BadRequestException("No lawyer is currently assigned to Case #" + caseId);
        }

        String lawyerName = removedLawyer.getName();
        Long lawyerUserId = removedLawyer.getUserId();

        // Perform the removal
        c.setLawyer(null);
        Case savedCase = caseRepo.save(c);

        log.info("Lawyer {} removed from Case #{}", lawyerName, caseId);

        // Audit log - attributed to the citizen's user for the record
        auditLogService.recordSystemAction(
                savedCase.getCitizen().getUser().getUserId(),
                "LAWYER_REMOVED",
                "Case:" + caseId + " - Removed lawyer: " + lawyerName
        );

        // Notify Citizen
        sendNotification(savedCase.getCitizen().getUser().getUserId(), caseId,
                "The lawyer " + lawyerName + " has been removed from your case \"" + savedCase.getTitle() + "\" (Case #" + caseId + "). " +
                        "The court clerk will assign a new lawyer shortly.");

        // Notify the Lawyer who was removed
        sendNotification(lawyerUserId, caseId,
                "You have been removed as the lawyer for Case #" + caseId + ": \"" + savedCase.getTitle() + "\". " +
                        "Please contact the court clerk if you believe this is an error.");

        return modelMapper.map(savedCase, CaseResponse.class);
    }

    @Override
    @Transactional
    public CaseResponse assignJudgeToCase(Long caseId, Long judgeId) {
        log.info("Assigning judgeId={} to caseId={}", judgeId, caseId);
        Case c = caseRepo.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found: " + caseId));
        User judge = userRepo.findById(judgeId)
                .orElseThrow(() -> new ResourceNotFoundException("Judge not found: " + judgeId));

        // Validation: Check if the user is actually a Judge
        if (judge.getRole() != User.Role.JUDGE) {
            throw new IllegalArgumentException("User with ID " + judgeId + " is not a Judge.");
        }

        c.setJudge(judge);
        Case savedCase  = caseRepo.save(c);

        log.info("Judge {} assigned to Case #{}", judge.getName(), caseId);

        // Audit log
        auditLogService.recordSystemAction(judge.getUserId(), "JUDGE_ASSIGNED",
                "Case:" + caseId + " - Judge: " + judge.getName());


        // Notify Judge
        sendNotification(judge.getUserId(), caseId,
                "You have been assigned as the Judge for Case #" + caseId + ": \"" + savedCase.getTitle() + "\". " +
                        "Citizen: " + savedCase.getCitizen().getName() +
                        (savedCase.getLawyer() != null ? ", Lawyer: " + savedCase.getLawyer().getName() : "") + ". " +
                        "Please review the case documents and schedule hearings as required.");
        // notify citizen
        sendNotification(savedCase.getCitizen().getUser().getUserId(), caseId,
                "A judge has been assigned to your case \"" + savedCase.getTitle() + "\" (Case #" + caseId + "). " +
                        "Judge: " + judge.getName() + ". Your case will proceed to hearing scheduling soon.");

        // Notify lawyer if assigned
        if (savedCase.getLawyer() != null) {
            sendNotification(savedCase.getLawyer().getUserId(), caseId,
                    "Judge " + judge.getName() + " has been assigned to Case #" + caseId + ": \"" + savedCase.getTitle() + "\". " +
                            "Please prepare your case arguments and stay updated on hearing schedules.");
        }

        return modelMapper.map(c, CaseResponse.class);
    }

    @Override
    @Transactional
    public DocumentResponse uploadCaseDocument(Long caseId, DocumentRequest request) {
        log.info("Adding document to caseId={}, docType={}", caseId, request.getDocType());
        Case c = caseRepo.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found: " + caseId));

        // Prevent adding evidence/docs to a finished case
        if (c.getStatus() == Case.CaseStatus.CLOSED) {
            throw new BadRequestException("Cannot add documents to a CLOSED case.");
        }

        CaseDocument caseDoc = modelMapper.map(request, CaseDocument.class);
        caseDoc.setCaseEntity(c);
        CaseDocument savedCaseDoc = docRepo.save(caseDoc);

        auditLogService.recordSystemAction(c.getCitizen().getUser().getUserId(),
                "CASE_DOCUMENT_ADDED",
                "Case:" + caseId + " - DocType: " + request.getDocType());

        return modelMapper.map(savedCaseDoc, DocumentResponse.class);
    }

    @Override
    public List<DocumentResponse> getCaseDocumentsByCaseId(Long caseId) {
        Case c = caseRepo.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found: " + caseId));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();

        boolean isStaff = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                        a.getAuthority().equals("ROLE_CLERK"));

        boolean isCitizenOwner = c.getCitizen().getUser().getEmail().equals(currentUserEmail);
        boolean isAssignedLawyer = (c.getLawyer() != null && c.getLawyer().getEmail().equals(currentUserEmail));
        boolean isAssignedJudge = (c.getJudge() != null && c.getJudge().getEmail().equals(currentUserEmail));

        if (!isStaff && !isCitizenOwner && !isAssignedLawyer && !isAssignedJudge) {
            log.warn("Unauthorized access attempt: User {} tried to view documents for Case #{}", currentUserEmail, caseId);
            throw new AccessDeniedException("Access Denied: You are not authorized to view documents for this case.");
        }

        return docRepo.findByCaseEntityCaseId(caseId).stream()
                .map(doc -> modelMapper.map(doc, DocumentResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DocumentResponse verifyCaseDocument(Long docId, CitizenDocument.VerificationStatus status) {
        log.info("Verifying document docId={} status={}", docId, status);
        CaseDocument caseDoc = docRepo.findById(docId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + docId));

        caseDoc.setVerificationStatus(status);
        CaseDocument savedCaseDoc = docRepo.save(caseDoc);

        auditLogService.recordSystemAction(caseDoc.getCaseEntity().getCitizen().getUser().getUserId(),
                "CASE_DOCUMENT_" + status.name(),
                "CaseDocument:" + docId + " for Case:" + caseDoc.getCaseEntity().getCaseId());

        return modelMapper.map(savedCaseDoc, DocumentResponse.class);
    }


    private void sendNotification(Long userId, Long entityId, String message) {
        NotificationRequest n = new NotificationRequest();
        n.setUserId(userId);
        n.setEntityId(entityId);// entity id
        n.setMessage(message);
        n.setCategory(Notification.NotificationCategory.CASE);

        notificationService.dispatchNotification(n);
    }
}