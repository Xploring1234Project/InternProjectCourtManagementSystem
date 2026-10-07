package com.justiceserve.judgmentservice.service.impl;

import com.justiceserve.judgmentservice.dto.CourtOrderRequest;
import com.justiceserve.judgmentservice.dto.CourtOrderResponse;
import com.justiceserve.judgmentservice.dto.JudgmentRequest;
import com.justiceserve.judgmentservice.dto.JudgmentResponse;
import com.justiceserve.judgmentservice.entity.CourtOrder;
import com.justiceserve.judgmentservice.entity.Judgment;
import com.justiceserve.judgmentservice.exception.BadRequestException;
import com.justiceserve.judgmentservice.exception.ResourceNotFoundException;
import com.justiceserve.judgmentservice.feignclient.*;
import com.justiceserve.judgmentservice.repository.CourtOrderRepository;
import com.justiceserve.judgmentservice.repository.JudgmentRepository;
import com.justiceserve.judgmentservice.service.JudgmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class JudgmentServiceImpl implements JudgmentService {

    private final JudgmentRepository judgmentRepo;
    private final CourtOrderRepository courtOrderRepo;
    private final UserFeignClient userClient;
    private final CaseFeignClient caseClient;
    private final CitizenFeignClient citizenClient;
    private final AuditFeignClient auditClient;
    private final NotificationFeignClient notificationClient;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public JudgmentResponse recordNewJudgment(JudgmentRequest request) {
        var c = caseClient.getCaseById(request.getCaseId());
        if (c == null) {
            throw new ResourceNotFoundException("Case not found with ID: " + request.getCaseId());
        }
        var judge = userClient.getUserById(request.getJudgeId());
        if (judge == null) {
            throw new ResourceNotFoundException("User not found with ID: " + request.getJudgeId());
        }
        if (!"JUDGE".equals(judge.role())) {
            throw new BadRequestException("User with ID " + request.getJudgeId() + " is not a judge");
        }

        // Fix: Use .equals() for string comparison
        if ("CLOSED".equalsIgnoreCase(c.status()) || "FILED".equalsIgnoreCase(c.status())) {
            throw new BadRequestException("Cannot draft judgment for case in status: " + c.status());
        }

        caseClient.updateCaseStatus(request.getCaseId(), "JUDGMENT_PENDING");

        Judgment judgment = modelMapper.map(request, Judgment.class);
        judgment.setStatus(Judgment.JudgmentStatus.DRAFT);
        Judgment savedJudgment = judgmentRepo.save(judgment);

        log.info("Judgment #{} recorded (DRAFT) for Case #{}", savedJudgment.getJudgmentId(), c.caseId());

        auditClient.log(judge.userId(), "JUDGMENT_RECORDED",
                String.format("Judgment ID: %d recorded as DRAFT for Case ID: %d", savedJudgment.getJudgmentId(), c.caseId()));

        var citizen = citizenClient.getCitizenById(c.citizenId());
        if (citizen != null) {
            sendNotification(citizen.userId(), savedJudgment.getJudgmentId(), "A judgment has been recorded (DRAFT) for your case \"" + c.title() + "\" (Case #" + c.caseId() + "). " +
                    "The final judgment will be announced shortly.");
        }

        return enrichJudgment(savedJudgment);
    }

    @Override
    public JudgmentResponse fetchJudgmentById(Long id) {
        Judgment judgment = judgmentRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Judgment not found with ID: " + id));
        return enrichJudgment(judgment);
    }

    @Override
    public List<JudgmentResponse> fetchAllJudgments() {
        return judgmentRepo.findAll().stream()
                .map(this::enrichJudgment).toList();
    }

    @Override
    public List<JudgmentResponse> fetchJudgmentsByCaseId(Long caseId) {
        return judgmentRepo.findByCaseId(caseId).stream()
                .map(this::enrichJudgment).toList();
    }

    @Override
    @Transactional
    public JudgmentResponse finalizeLegalJudgment(Long id) {
        Judgment judgment = judgmentRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Judgment not found with ID: " + id));
        if (judgment.getStatus() == Judgment.JudgmentStatus.FINAL) {
            throw new BadRequestException("Judgment with ID " + id + " is already finalized");
        }

        judgment.setStatus(Judgment.JudgmentStatus.FINAL);
        Judgment savedJudgment = judgmentRepo.save(judgment);

        caseClient.updateCaseStatus(savedJudgment.getCaseId(), "CLOSED");

        log.info("Judgment #{} finalized for Case #{}", savedJudgment.getJudgmentId(), savedJudgment.getCaseId());
        var c = caseClient.getCaseById(savedJudgment.getCaseId());
        if (c == null) {
            throw new ResourceNotFoundException("Case not found with ID: " + savedJudgment.getCaseId());
        }
        var citizen = citizenClient.getCitizenById(c.citizenId());
        if (citizen == null) {
            throw new ResourceNotFoundException("Citizen not found with ID: " + c.citizenId());
        }

        sendNotification(citizen.userId(), savedJudgment.getJudgmentId(),
                "The judgment for your case \"" + c.title() + "\" (Case #" + c.caseId() + ") has been finalized. " +
                        "Your case is now CLOSED.");

        if (c.lawyerId() != null) {
            sendNotification(c.lawyerId(), savedJudgment.getJudgmentId(),
                    "Judgment finalized for Case #" + c.caseId() + " \"" + c.title() + "\". Case is now CLOSED.");
        }

        auditClient.log(judgment.getJudgeId(), "JUDGMENT_FINALIZED",
                String.format("Judgment ID: %d finalized for Case ID: %d", savedJudgment.getJudgmentId(), savedJudgment.getCaseId()));

        return enrichJudgment(savedJudgment);
    }

    @Override
    @Transactional
    public CourtOrderResponse issueNewCourtOrder(CourtOrderRequest request) {
        var c = caseClient.getCaseById(request.getCaseId());
        if (c == null) {
            throw new ResourceNotFoundException("Case not found with ID: " + request.getCaseId());
        }
        var judge  = userClient.getUserById(request.getJudgeId());
        if (judge == null) {
            throw new ResourceNotFoundException("User not found with ID: " + request.getJudgeId());
        }
        if(!"JUDGE".equals(judge.role())){
            throw new BadRequestException("User with ID " + request.getJudgeId() + " is not a judge");
        }
        if("CLOSED".equals(c.status())){
            throw new BadRequestException("Cannot draft judgment for case in status: " + c.status());
        }

        CourtOrder order = modelMapper.map(request, CourtOrder.class);
        CourtOrder savedCourtOrder = courtOrderRepo.save(order);
        log.info("Court Order #{} issued for Case #{}", savedCourtOrder.getOrderId(), c.caseId());

        auditClient.log(judge.userId(), "COURT_ORDER_ISSUED",
                String.format("Court Order ID: %d issued for Case ID: %d", savedCourtOrder.getOrderId(), c.caseId()));

        var citizen = citizenClient.getCitizenById(c.citizenId());
        if (citizen == null) {
            throw new ResourceNotFoundException("Citizen not found with ID: " + c.citizenId());
        }


        sendNotification(citizen.userId(), savedCourtOrder.getOrderId(),
                "A new court order has been issued for your case \"" + c.title() + "\" (Case #" + c.caseId() + "). " +
                        "Please review the order details and comply accordingly.");



        sendNotification(c.lawyerId(), savedCourtOrder.getOrderId(),
                "A new court order has been issued for Case #" + c.caseId() + " \"" + c.title() + "\". " +
                        "Please review the order details and advise your client accordingly.");


        return modelMapper.map(savedCourtOrder, CourtOrderResponse.class);
    }

    @Override
    public CourtOrderResponse fetchCourtOrderById(Long id) {
        CourtOrder order = courtOrderRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Court Order not found with ID: " + id));

        return modelMapper.map(order, CourtOrderResponse.class);
    }

    @Override
    public List<CourtOrderResponse> fetchAllCourtOrders() {
        return courtOrderRepo.findAll().stream()
                .map(order  -> modelMapper.map(order, CourtOrderResponse.class)).toList();
    }

    @Override
    public List<CourtOrderResponse> fetchCourtOrdersByCaseId(Long caseId) {
        return courtOrderRepo.findByCaseId(caseId).stream()
                .map(order -> modelMapper.map(order, CourtOrderResponse.class)).toList();
    }

    @Override
    @Transactional
    public CourtOrderResponse updateCourtOrderStatus(Long id, CourtOrder.OrderStatus status) {
        CourtOrder order =  courtOrderRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Court Order not found with ID: " + id));
        order.setStatus(status);
        CourtOrder savedOrder = courtOrderRepo.save(order);

        auditClient.log(order.getJudgeId(), "COURT_ORDER_STATUS_UPDATED",
                String.format("Court Order ID: %d status updated to %s for Case ID: %d", savedOrder.getOrderId(), status, savedOrder.getCaseId()));


        return modelMapper.map(savedOrder, CourtOrderResponse.class);
    }

    private JudgmentResponse enrichJudgment(Judgment j) {
        JudgmentResponse response = modelMapper.map(j, JudgmentResponse.class);
        try {
            var c = caseClient.getCaseById(j.getCaseId());
            if (c != null) response.setCaseTitle(c.title());
        } catch (Exception e) {
            log.error("Failed to enrich judgment with case title: {}", e.getMessage());
        }
        return response;
    }


    private void sendNotification(Long userId, Long entityId, String message) {
        try {
            notificationClient.send(Map.of("userId", userId, "entityId", entityId, "category", "JUDGMENT", "message", message));
        } catch (Exception e) {
            log.error("Failed to send notification to user {}: {}", userId, e.getMessage());
        }
    }
}
