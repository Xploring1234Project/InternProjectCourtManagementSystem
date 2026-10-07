package com.justiceserve.hearingservice.service.impl;

import com.justiceserve.hearingservice.dto.HearingRequest;
import com.justiceserve.hearingservice.dto.HearingResponse;
import com.justiceserve.hearingservice.dto.ProceedingRequest;
import com.justiceserve.hearingservice.dto.ProceedingResponse;
import com.justiceserve.hearingservice.entity.Hearing;
import com.justiceserve.hearingservice.entity.Proceeding;
import com.justiceserve.hearingservice.exception.BadRequestException;
import com.justiceserve.hearingservice.exception.ResourceNotFoundException;
import com.justiceserve.hearingservice.feignclient.*;
import com.justiceserve.hearingservice.repository.HearingRepository;
import com.justiceserve.hearingservice.repository.ProceedingRepository;
import com.justiceserve.hearingservice.service.HearingService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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
public class HearingServiceImpl implements HearingService {

    private final HearingRepository hearingRepo;
    private final ProceedingRepository proceedingRepo;
    private final UserFeignClient userClient;
    private final CaseFeignClient caseClient;
    private final CitizenFeignClient citizenClient;
    private final AuditFeignClient auditClient;
    private final NotificationFeignClient notificationClient;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public HearingResponse scheduleNewHearing(HearingRequest request) {
        log.info("Scheduling hearing caseId={}, judgeId={}", request.getCaseId(), request.getJudgeId());

        var c = caseClient.getCaseById(request.getCaseId());
        if (c == null) {
            throw new ResourceNotFoundException("Case not found: " + request.getCaseId());
        }

        if("CLOSED".equals(c.status())){
            throw new BadRequestException("Case is already closed");
        }

        var judge = userClient.getUserById(request.getJudgeId());
        if (judge == null) {
            throw new ResourceNotFoundException("Judge not found: " + request.getJudgeId());
        }

        if (!"JUDGE".equals(judge.role())) {
            throw new BadRequestException("User is not a JUDGE: " + request.getJudgeId());
        }

        Hearing hearing = modelMapper.map(request, Hearing.class);
        hearing.setStatus(Hearing.HearingStatus.SCHEDULED);
        Hearing savedHearing = hearingRepo.save(hearing);

        // Update Case Status
        caseClient.updateCaseStatus(request.getCaseId(), "HEARING_SCHEDULED");

        // Audit Logging
        auditClient.log(judge.userId(), "HEARING_SCHEDULED",
                "Hearing:" + savedHearing.getHearingId() + " Case:" + request.getCaseId());

        // Notifications
        var citizen = citizenClient.getCitizenById(c.citizenId());
        if(citizen != null) {
            sendNotification(citizen.userId(), savedHearing.getHearingId(),
                    "Hearing for case \"" + c.title() + "\" scheduled on " + request.getDate() +
                            " at " + request.getTime() + ". Judge: " + judge.name());
        }

        if(c.lawyerId() != null) {
            sendNotification(c.lawyerId(), savedHearing.getHearingId(),
                    "Hearing for case \"" + c.title() + "\" scheduled on " + request.getDate() +
                            " at " + request.getTime() + ". Judge: " + judge.name());
        }

        return enrich(savedHearing);
    }

    @Override
    public HearingResponse getHearingById(Long id) {
        Hearing hearing = hearingRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hearing not found with ID: " + id));
        return enrich(hearing);
    }

    @Override
    public List<HearingResponse> getAllHearings() {
        return hearingRepo.findAll().stream()
                .map(this::enrich)
                .collect(Collectors.toList());
    }

    @Override
    public List<HearingResponse> getHearingsByCaseId(Long caseId) {
        return hearingRepo.findByCaseId(caseId).stream()
                .map(this::enrich)
                .collect(Collectors.toList());
    }

    @Override
    public List<HearingResponse> getHearingsByJudgeId(Long judgeId) {
        return hearingRepo.findByJudgeId(judgeId).stream()
                .map(this::enrich)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HearingResponse updateHearingStatus(Long id, Hearing.HearingStatus status) {
        log.info("Updating hearing #{} status to {}", id, status);
        Hearing hearing = hearingRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hearing not found: " + id));

        Hearing.HearingStatus oldStatus = hearing.getStatus();
        hearing.setStatus(status);
        Hearing savedHearing = hearingRepo.save(hearing);

        auditClient.log(savedHearing.getJudgeId(), "HEARING_STATUS_UPDATED",
                "Hearing:" + id + " [" + oldStatus + " -> " + status + "] for Case:" + savedHearing.getCaseId());

        var c = caseClient.getCaseById(savedHearing.getCaseId());
        if (c != null) {
            var citizen = citizenClient.getCitizenById(c.citizenId());
            if (citizen != null) {
                sendNotification(citizen.userId(), id,
                        "Hearing #" + id + " for your case \"" + c.title() + "\" status updated to: " + status.name());
            }
        }

        return enrich(savedHearing);
    }

    @Override
    @Transactional
    public ProceedingResponse recordHearingProceeding(ProceedingRequest request) {
        log.info("Adding proceeding for hearingId={}", request.getHearingId());
        Hearing hearing = hearingRepo.findById(request.getHearingId())
                .orElseThrow(() -> new ResourceNotFoundException("Hearing not found: " + request.getHearingId()));

        Proceeding proceeding = modelMapper.map(request, Proceeding.class);
        proceeding.setHearing(hearing);
        Proceeding savedProceeding = proceedingRepo.save(proceeding);

        auditClient.log(hearing.getJudgeId(), "PROCEEDING_ADDED",
                "Proceeding:" + savedProceeding.getProceedingId() + " for Hearing:" + hearing.getHearingId());

        ProceedingResponse response = modelMapper.map(savedProceeding, ProceedingResponse.class);
        response.setHearingId(savedProceeding.getHearing().getHearingId());
        return response;
    }

    @Override
    public List<ProceedingResponse> fetchProceedingsByHearingId(Long hearingId) {
        return proceedingRepo.findByHearingHearingId(hearingId).stream()
                .map(p -> modelMapper.map(p, ProceedingResponse.class))
                .collect(Collectors.toList());
    }


    private HearingResponse enrich(Hearing h) {
        HearingResponse response = modelMapper.map(h, HearingResponse.class);

        // 1. Fetch Case Details (Title, Citizen ID, Lawyer ID)
        try {
            var c = caseClient.getCaseById(h.getCaseId());
            if (c != null) {
                response.setCaseTitle(c.title());
                response.setCitizenId(c.citizenId());
                response.setLawyerId(c.lawyerId());
            }
        } catch (Exception e) {
            log.warn("Could not enrich Case details for Case ID {}: {}", h.getCaseId(), e.getMessage());
        }

        // 2. Fetch Judge Details (Name)
        if (h.getJudgeId() != null) {
            try {
                var judge = userClient.getUserById(h.getJudgeId());
                if (judge != null) {
                    response.setJudgeName(judge.name());
                }
            } catch (Exception e) {
                log.warn("Could not enrich Judge name for Judge ID {}: {}", h.getJudgeId(), e.getMessage());
            }
        }

        return response;
    }

    private void sendNotification(Long userId, Long entityId, String message) {
        try {
            notificationClient.send(Map.of("userId", userId, "entityId", entityId, "category", "HEARING", "message", message));
        } catch (Exception e) {
            log.error("Failed to send notification to user {}: {}", userId, e.getMessage());
        }
    }
}