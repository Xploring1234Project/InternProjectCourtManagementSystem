package com.justiceserve.caseservice.service.impl;

import com.justiceserve.caseservice.dto.CaseRequest;
import com.justiceserve.caseservice.dto.CaseResponse;
import com.justiceserve.caseservice.dto.DocumentRequest;
import com.justiceserve.caseservice.dto.DocumentResponse;
import com.justiceserve.caseservice.entity.Case;
import com.justiceserve.caseservice.entity.CaseDocument;
import com.justiceserve.caseservice.exception.BadRequestException;
import com.justiceserve.caseservice.exception.ResourceNotFoundException;
import com.justiceserve.caseservice.feignclient.AuditFeignClient;
import com.justiceserve.caseservice.feignclient.CitizenFeignClient;
import com.justiceserve.caseservice.feignclient.NotificationFeignClient;
import com.justiceserve.caseservice.feignclient.UserFeignClient;
import com.justiceserve.caseservice.repository.CaseDocumentRepository;
import com.justiceserve.caseservice.repository.CaseRepository;
import com.justiceserve.caseservice.service.CaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaseServiceImpl implements CaseService {

    private final CaseRepository caseRepo;
    private final CaseDocumentRepository docRepo;
    private final CitizenFeignClient citizenClient;
    private final UserFeignClient userClient;
    private final AuditFeignClient auditClient;
    private final NotificationFeignClient notificationClient;
    private final ModelMapper modelMapper;


    @Override
    @Transactional
    public CaseResponse fileNewCase(CaseRequest request) {
        log.info("Filing case for citizenId={}, lawyerId={}", request.getCitizenId(), request.getLawyerId());

        var citizen = citizenClient.getCitizenById(request.getCitizenId());
        if (citizen == null) {
            throw new ResourceNotFoundException("Citizen not found: " + request.getCitizenId());
        }

        if (request.getLawyerId() != null) {
            var lawyer = userClient.getUserById(request.getLawyerId());
            if (lawyer == null || !"LAWYER".equals(lawyer.role())) {
                throw new BadRequestException("User is not a valid Lawyer: " + request.getLawyerId());
            }
        }

        Case newCase = modelMapper.map(request, Case.class);
        newCase.setStatus(Case.CaseStatus.FILED);
        Case savedCase = caseRepo.save(newCase);

        auditClient.log(citizen.userId(), "CASE_FILED", "Case:" + savedCase.getCaseId());

        // Notifications
        if (savedCase.getLawyerId() != null) {
            sendNotification(citizen.userId(), savedCase.getCaseId(), "Your case has been filed with a linked lawyer.");
            sendNotification(savedCase.getLawyerId(), savedCase.getCaseId(), "You have been selected for Case #" + savedCase.getCaseId());
        } else {
            sendNotification(citizen.userId(), savedCase.getCaseId(), "Case filed successfully. Clerk will assign a lawyer soon.");
        }

        return enrich(savedCase);
    }

    @Override
    public CaseResponse getCaseById(Long id) {
        Case c = caseRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found: " + id));
        return enrich(c);
    }

    @Override
    public List<CaseResponse> getAllCases() {
        return caseRepo.findAll().stream()
                .map(this::enrich)
                .collect(Collectors.toList());
    }

    @Override
    public List<CaseResponse> getCasesByCitizenId(Long citizenId) {
        return caseRepo.findByCitizenId(citizenId).stream()
                .map(this::enrich)
                .collect(Collectors.toList());
    }

    @Override
    public List<CaseResponse> getCasesByLawyerId(Long lawyerId) {
        return caseRepo.findByLawyerId(lawyerId).stream()
                .map(this::enrich)
                .collect(Collectors.toList());
    }

    @Override
    public List<CaseResponse> getCasesByJudgeId(Long judgeId) {
        return caseRepo.findByJudgeId(judgeId).stream()
                .map(this::enrich)
                .collect(Collectors.toList());
    }

    @Override
    public List<CaseResponse> getCasesByStatus(Case.CaseStatus status) {
        return caseRepo.findByStatus(status).stream()
                .map(this::enrich)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CaseResponse updateCaseStatus(Long id, Case.CaseStatus status) {
        Case c = caseRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found: " + id));

        if (c.getStatus() == Case.CaseStatus.CLOSED) {
            throw new BadRequestException("Cannot update status: Case is already CLOSED.");
        }

        c.setStatus(status);
        Case updatedCase = caseRepo.save(c);

        var citizen = citizenClient.getCitizenById(updatedCase.getCitizenId());
        auditClient.log(citizen.userId(), "CASE_STATUS_UPDATED", "Case:" + id + " to " + status);
        sendNotification(citizen.userId(), id, "Case status updated to: " + status);

        return enrich(updatedCase);
    }

    @Override
    @Transactional
    public CaseResponse assignLawyerToCase(Long caseId, Long lawyerId) {
        Case c = caseRepo.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found: " + caseId));

        var lawyer = userClient.getUserById(lawyerId);
        if (lawyer == null || !"LAWYER".equals(lawyer.role())) {
            throw new BadRequestException("User ID " + lawyerId + " is not a valid Lawyer.");
        }

        c.setLawyerId(lawyer.userId());
        Case savedCase = caseRepo.save(c);

        auditClient.log(lawyer.userId(), "LAWYER_ASSIGNED", "Case:" + caseId);

        var citizen = citizenClient.getCitizenById(savedCase.getCitizenId());
        sendNotification(lawyer.userId(), caseId, "Assigned to Case #" + caseId);
        //sendNotification(citizen.userId(), caseId, "Lawyer " + lawyer.name() + " assigned to your case.");

        return enrich(savedCase);
    }

    @Override
    @Transactional
    public CaseResponse removeLawyerFromCase(Long caseId) {
        Case c = caseRepo.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found: " + caseId));

        if (c.getLawyerId() == null) {
            throw new BadRequestException("No lawyer assigned to Case #" + caseId);
        }

        Long oldLawyerId = c.getLawyerId();
        c.setLawyerId(null);
        Case savedCase = caseRepo.save(c);

        var citizen = citizenClient.getCitizenById(savedCase.getCitizenId());
        auditClient.log(citizen.userId(), "LAWYER_REMOVED", "Case:" + caseId);

        sendNotification(citizen.userId(), caseId, "Lawyer removed from your case.");
        sendNotification(oldLawyerId, caseId, "You have been removed from Case #" + caseId);

        return enrich(savedCase);
    }

    @Override
    @Transactional
    public CaseResponse assignJudgeToCase(Long caseId, Long judgeId) {
        Case c = caseRepo.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found: " + caseId));

        var judge = userClient.getUserById(judgeId);
        if (judge == null || !"JUDGE".equals(judge.role())) {
            throw new BadRequestException("User ID " + judgeId + " is not a Judge.");
        }

        c.setJudgeId(judge.userId());
        Case savedCase = caseRepo.save(c);

        auditClient.log(judge.userId(), "JUDGE_ASSIGNED", "Case:" + caseId);

        var citizen = citizenClient.getCitizenById(savedCase.getCitizenId());

        sendNotification(judge.userId(), caseId, "Assigned as Judge for Case #" + caseId);

        //sendNotification(citizen.userId(), caseId, "Judge " + judge.name() + " assigned to your case #" + caseId);

        sendNotification(savedCase.getLawyerId(), caseId, "Judge " + judge.name() + " assigned to your case #" +caseId);

        return enrich(savedCase);
    }

    @Override
    @Transactional
    public DocumentResponse uploadCaseDocument(Long caseId, DocumentRequest request) {
        Case c = caseRepo.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found: " + caseId));

        if (c.getStatus() == Case.CaseStatus.CLOSED) {
            throw new BadRequestException("Cannot add documents to a CLOSED case.");
        }

        CaseDocument caseDoc = modelMapper.map(request, CaseDocument.class);
        caseDoc.setCaseEntity(c);
        CaseDocument savedCaseDoc = docRepo.save(caseDoc);

        auditClient.log(c.getCitizenId(), "CASE_DOCUMENT_ADDED", "Case:" + caseId);

        return modelMapper.map(savedCaseDoc, DocumentResponse.class);
    }

    @Override
    public List<DocumentResponse> getCaseDocumentsByCaseId(Long caseId) {
        if (!caseRepo.existsById(caseId)) {
            throw new ResourceNotFoundException("Case not found: " + caseId);
        }
        return docRepo.findByCaseEntityCaseId(caseId).stream()
                .map(doc -> modelMapper.map(doc, DocumentResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DocumentResponse verifyCaseDocument(Long docId, CaseDocument.VerificationStatus status) {
        CaseDocument caseDoc = docRepo.findById(docId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + docId));

        caseDoc.setVerificationStatus(status);
        CaseDocument savedCaseDoc = docRepo.save(caseDoc);

        return modelMapper.map(savedCaseDoc, DocumentResponse.class);
    }


    private CaseResponse enrich(Case c) {
        CaseResponse r = modelMapper.map(c, CaseResponse.class);

        // Enrich Citizen Name
        try {
            var citizen = citizenClient.getCitizenById(c.getCitizenId());
            if (citizen != null) r.setCitizenName(citizen.name());
        } catch (Exception e) {
            log.debug("Cannot enrich citizenName for citizenId {}: {}", c.getCitizenId(), e.getMessage());
        }

        // Enrich Lawyer Name
        if (c.getLawyerId() != null) {
            try {
                var lawyer = userClient.getUserById(c.getLawyerId());
                if (lawyer != null) r.setLawyerName(lawyer.name());
            } catch (Exception e) {
                log.debug("Cannot enrich lawyerName for lawyerId {}: {}", c.getLawyerId(), e.getMessage());
            }
        }

        // Enrich Judge Name
        if (c.getJudgeId() != null) {
            try {
                var judge = userClient.getUserById(c.getJudgeId());
                if (judge != null) r.setJudgeName(judge.name());
            } catch (Exception e) {
                log.debug("Cannot enrich judgeName for judgeId {}: {}", c.getJudgeId(), e.getMessage());
            }
        }
        return r;
    }

    private void sendNotification(Long userId, Long entityId, String message) {
        try {
            notificationClient.send(Map.of("userId", userId, "entityId", entityId, "category", "CASE", "message", message));
        } catch (Exception e) {
            log.error("Failed to send notification to user {}: {}", userId, e.getMessage());
        }
    }
}