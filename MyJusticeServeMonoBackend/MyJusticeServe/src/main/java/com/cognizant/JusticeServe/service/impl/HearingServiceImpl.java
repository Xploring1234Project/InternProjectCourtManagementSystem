package com.cognizant.JusticeServe.service.impl;

import com.cognizant.JusticeServe.dto.request.HearingRequest;
import com.cognizant.JusticeServe.dto.request.NotificationRequest;
import com.cognizant.JusticeServe.dto.request.ProceedingRequest;
import com.cognizant.JusticeServe.dto.response.HearingResponse;
import com.cognizant.JusticeServe.dto.response.ProceedingResponse;
import com.cognizant.JusticeServe.entity.*;
import com.cognizant.JusticeServe.exception.ResourceNotFoundException;
import com.cognizant.JusticeServe.repository.CaseRepository;
import com.cognizant.JusticeServe.repository.HearingRepository;
import com.cognizant.JusticeServe.repository.ProceedingRepository;
import com.cognizant.JusticeServe.repository.UserRepository;
import com.cognizant.JusticeServe.service.AuditLogService;
import com.cognizant.JusticeServe.service.HearingService;
import com.cognizant.JusticeServe.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HearingServiceImpl implements HearingService {

    private final HearingRepository hearingRepo;
    private final ProceedingRepository proceedingRepo;
    private final CaseRepository caseRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public HearingResponse scheduleNewHearing(HearingRequest request) {
        log.info("Scheduling hearing for caseId={}, judgeId={}, date={}", request.getCaseId(), request.getJudgeId(), request.getDate());
        Case caseEntity = caseRepo.findById(request.getCaseId()).orElseThrow(() -> new ResourceNotFoundException("Case not found with ID: " + request.getCaseId()));
        User judge = userRepo.findById(request.getJudgeId()).orElseThrow(() -> new ResourceNotFoundException("Judge not found with ID: " + request.getJudgeId()));

        Hearing hearing = modelMapper.map(request, Hearing.class);
        hearing.setJudge(judge);
        hearing.setCaseEntity(caseEntity);
        Hearing savedHearing = hearingRepo.save(hearing);

        caseEntity.setStatus(Case.CaseStatus.HEARING_SCHEDULED);
        caseRepo.save(caseEntity);
        log.info("Hearing #{} scheduled for Case #{} on {}", savedHearing.getHearingId(), caseEntity.getCaseId(), request.getDate());

        auditLogService.recordSystemAction(
                judge.getUserId(),
                "HEARING_SCHEDULED",
                String.format("Hearing ID: %d scheduled for Case ID: %d on %s",
                        savedHearing.getHearingId(),
                        caseEntity.getCaseId(),
                        savedHearing.getDate())
        );

        String msg = "A hearing has been scheduled for your case \"" + caseEntity.getTitle() + "\" (Case #" + caseEntity.getCaseId() + "). " +
                "Date: " + savedHearing.getDate() + " | Time: " + savedHearing.getTime() + " | Judge: " + judge.getName() + ". " +
                "Please be present or ensure your lawyer is present.";

        sendNotification(caseEntity.getCitizen().getUser().getUserId(), savedHearing.getHearingId(),msg);

        if (caseEntity.getLawyer() != null) {
            sendNotification(caseEntity.getLawyer().getUserId(), savedHearing.getHearingId(),
                    "Hearing scheduled for Case #" + caseEntity.getCaseId() + " \"" + caseEntity.getTitle() + "\". " +
                            "Date: " + request.getDate() + " | Time: " + request.getTime() + " | Judge: " + judge.getName() + ". " +
                            "Please prepare your arguments and be present.");
        }

        return modelMapper.map(savedHearing, HearingResponse.class);
    }

    @Override
    public HearingResponse getHearingById(Long Id) {
        Hearing hearing = hearingRepo.findById(Id).orElseThrow(() -> new ResourceNotFoundException("Hearing not found" + Id));

        return modelMapper.map(hearing, HearingResponse.class);
    }

    @Override
    public List<HearingResponse> getAllHearings() {
        return hearingRepo.findAll().stream()
                .map(hearing -> modelMapper.map(hearing, HearingResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<HearingResponse> getHearingsByCaseId(Long caseId) {
        return hearingRepo.findByCaseEntityCaseId(caseId).stream()
                .map(hearing -> modelMapper.map(hearing, HearingResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<HearingResponse> getHearingsByJudgeId(Long judgeId) {
        return hearingRepo.findByJudgeUserId(judgeId).stream()
                .map(hearing -> modelMapper.map(hearing, HearingResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HearingResponse updateHearingStatus(Long id, Hearing.HearingStatus status) {
        log.info("Updating hearing #{} status to {}", id, status);
        Hearing hearing = hearingRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Hearing not found" + id));
        Hearing.HearingStatus oldStatus = hearing.getStatus();
        hearing.setStatus(status);
        Hearing savedHearing = hearingRepo.save(hearing);

        auditLogService.recordSystemAction(savedHearing.getJudge().getUserId(),
                "HEARING_STATUS_UPDATED",
                "Hearing:" + id + " [" + oldStatus + " -> " + status + "] for Case:" + savedHearing.getCaseEntity().getCaseId());

        Case c =  savedHearing.getCaseEntity();
        sendNotification(c.getCitizen().getUser().getUserId(),id,
                "Hearing #" + id + " for your case \"" + c.getTitle() + "\" status updated to: " + status.name() + ".");

        return modelMapper.map(savedHearing, HearingResponse.class);
    }

    @Override
    @Transactional
    public ProceedingResponse recordHearingProceeding(ProceedingRequest request) {
        log.info("Adding proceeding for hearingId={}", request.getHearingId());
        Hearing hearing = hearingRepo.findById(request.getHearingId())
                .orElseThrow(() -> new ResourceNotFoundException("Hearing not found" + request.getHearingId()));
        Proceeding proceeding = modelMapper.map(request, Proceeding.class);
        proceeding.setHearing(hearing);

        Proceeding savedProceeding = proceedingRepo.save(proceeding);

        auditLogService.recordSystemAction(hearing.getJudge().getUserId(),
                "PROCEEDING_ADDED",
                "Proceeding:" + savedProceeding.getProceedingId() + " for Hearing:" + hearing.getHearingId());

        return modelMapper.map(savedProceeding, ProceedingResponse.class);
    }

    @Override
    public List<ProceedingResponse> fetchProceedingsByHearingId(Long hearingId) {
        return proceedingRepo.findByHearingHearingId(hearingId).stream()
                .map(hearing -> modelMapper.map(hearing, ProceedingResponse.class))
                .collect(Collectors.toList());
    }


    private void sendNotification(Long userId, Long entityId, String message) {
        NotificationRequest n = new NotificationRequest();
        n.setUserId(userId);
        n.setEntityId(entityId);// entity id
        n.setMessage(message);
        n.setCategory(Notification.NotificationCategory.HEARING);

        notificationService.dispatchNotification(n);
    }




}

