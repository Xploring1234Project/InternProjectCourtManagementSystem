package com.cognizant.JusticeServe.service.impl;

import com.cognizant.JusticeServe.dto.request.CourtOrderRequest;
import com.cognizant.JusticeServe.dto.request.JudgmentRequest;
import com.cognizant.JusticeServe.dto.request.NotificationRequest;
import com.cognizant.JusticeServe.dto.response.CourtOrderResponse;
import com.cognizant.JusticeServe.dto.response.JudgmentResponse;
import com.cognizant.JusticeServe.entity.*;
import com.cognizant.JusticeServe.exception.BadRequestException;
import com.cognizant.JusticeServe.exception.ResourceNotFoundException;
import com.cognizant.JusticeServe.repository.CaseRepository;
import com.cognizant.JusticeServe.repository.CourtOrderRepository;
import com.cognizant.JusticeServe.repository.JudgmentRepository;
import com.cognizant.JusticeServe.repository.UserRepository;
import com.cognizant.JusticeServe.service.AuditLogService;
import com.cognizant.JusticeServe.service.JudgmentService;
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
public class JudgmentServiceImpl implements JudgmentService {

    private final JudgmentRepository judgmentRepo;
    private final CourtOrderRepository orderRepo;
    private final CaseRepository caseRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public JudgmentResponse recordNewJudgment(JudgmentRequest request) {
        log.info("Recording judgment for caseId={}, judgeId={}", request.getCaseId(), request.getJudgeId());
        Case c = caseRepo.findById(request.getCaseId()).orElseThrow(() -> new ResourceNotFoundException("Case not found" + request.getCaseId()));
        User judge = userRepo.findById(request.getJudgeId()).orElseThrow(() -> new ResourceNotFoundException("Judge not found" + request.getJudgeId()));

        // Guard: Prevent drafting judgments for already closed or just filed cases
        if (c.getStatus() == Case.CaseStatus.CLOSED || c.getStatus() == Case.CaseStatus.FILED) {
            throw new BadRequestException("Judgment cannot be drafted. Case status: " + c.getStatus());
        }

        c.setStatus(Case.CaseStatus.JUDGEMENT_PENDING);
        caseRepo.save(c);

        Judgment judgment = modelMapper.map(request, Judgment.class);
        judgment.setCaseEntity(c);
        judgment.setJudge(judge);
        judgment.setStatus(Judgment.JudgmentStatus.DRAFT);
        Judgment savedJudgment = judgmentRepo.save(judgment);

        log.info("Judgment #{} recorded (DRAFT) for Case #{}", savedJudgment.getJudgmentId(), c.getCaseId());

        auditLogService.recordSystemAction(judge.getUserId(), "JUDGMENT_RECORDED",
                "Judgment:" + savedJudgment.getJudgmentId() + " (DRAFT) for Case:" + c.getCaseId());

        sendNotification(c.getCitizen().getUser().getUserId(), savedJudgment.getJudgmentId(),
                "A judgment has been recorded (DRAFT) for your case \"" + c.getTitle() + "\" (Case #" + c.getCaseId() + "). " +
                        "The final judgment will be announced shortly.");

        return modelMapper.map(savedJudgment, JudgmentResponse.class);
    }

    @Override
    public JudgmentResponse fetchJudgmentById(Long id) {
        Judgment judgment = judgmentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Judgment not found: " + id));
        return modelMapper.map(judgment, JudgmentResponse.class);
    }

    @Override
    public List<JudgmentResponse> fetchAllJudgments() {
        return judgmentRepo.findAll().stream()
                .map(judgment -> modelMapper.map(judgment, JudgmentResponse.class)).toList();
    }

    @Override
    public List<JudgmentResponse> fetchJudgmentsByCaseId(Long caseId) {
        return judgmentRepo.findByCaseEntityCaseId(caseId).stream()
                .map(j -> modelMapper.map(j, JudgmentResponse.class)).toList();
    }

    @Override
    @Transactional
    public JudgmentResponse finalizeLegalJudgment(Long id){
        log.info("Finalizing judgment #{}", id);
        Judgment judgment = judgmentRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Judgment not found: " + id));

        if (judgment.getStatus() == Judgment.JudgmentStatus.FINAL) {
            throw new BadRequestException("Judgment is already finalized.");
        }

        judgment.setStatus(Judgment.JudgmentStatus.FINAL);
        Judgment  savedJudgment = judgmentRepo.save(judgment);

        Case c = savedJudgment.getCaseEntity();
        c.setStatus(Case.CaseStatus.CLOSED);
        caseRepo.save(c);
        log.info("Judgment #{} finalized. Case #{} CLOSED.", id, c.getCaseId());

        sendNotification(c.getCitizen().getUser().getUserId(), savedJudgment.getJudgmentId(),
                "The judgment for your case \"" + c.getTitle() + "\" (Case #" + c.getCaseId() + ") has been finalized. " +
                        "Your case is now CLOSED. Please contact your lawyer or the court for the full judgment details.");

        if (c.getLawyer() != null) {
            sendNotification(c.getLawyer().getUserId(), savedJudgment.getJudgmentId(),
                    "Judgment finalized for Case #" + c.getCaseId() + " \"" + c.getTitle() + "\". " +
                            "Case is now CLOSED. Please inform your client and proceed with post-judgment formalities.");
        }

        if (c.getJudge() != null) {
            auditLogService.recordSystemAction(c.getJudge().getUserId(), "CASE_CLOSED",
                    "Case:" + c.getCaseId() + " closed via Judgment:" + id);
        }
        return modelMapper.map(savedJudgment, JudgmentResponse.class);
    }

    @Override
    @Transactional
    public CourtOrderResponse issueNewCourtOrder(CourtOrderRequest request){
        log.info("Issuing court order for caseId={}, judgeId={}", request.getCaseId(), request.getJudgeId());
        Case c = caseRepo.findById(request.getCaseId()).orElseThrow(() -> new ResourceNotFoundException("Case not found: " + request.getCaseId()));
        User judge = userRepo.findById(request.getJudgeId()).orElseThrow(() -> new ResourceNotFoundException("Judge not found: " + request.getJudgeId()));

        // Guard: Cannot issue orders for closed cases
        if (c.getStatus() == Case.CaseStatus.CLOSED) {
            throw new BadRequestException("Cannot issue court orders for a CLOSED case.");
        }

        CourtOrder order = modelMapper.map(request,CourtOrder.class);
        order.setCaseEntity(c);
        order.setJudge(judge);
        CourtOrder savedOrder = orderRepo.save(order);

        log.info("Court order #{} issued for Case #{}", savedOrder.getOrderId(), c.getCaseId());

        auditLogService.recordSystemAction(judge.getUserId(), "COURT_ORDER_ISSUED",
                "CourtOrder:" + savedOrder.getOrderId() + " for Case:" + c.getCaseId());

        sendNotification(c.getCitizen().getUser().getUserId(), savedOrder.getOrderId(),
                "A court order has been issued for your case \"" + c.getTitle() + "\" (Case #" + c.getCaseId() + "). " +
                        "Please read the order details carefully and comply with the instructions.");

        if (c.getLawyer() != null) {
            sendNotification(c.getLawyer().getUserId(), savedOrder.getOrderId(),
                    "Court Order #" + savedOrder.getOrderId() + " issued for Case #" + c.getCaseId() + ". " +
                            "Please review and ensure compliance.");
        }

        return modelMapper.map(savedOrder, CourtOrderResponse.class);
    }

    public CourtOrderResponse fetchCourtOrderById(Long id){
        CourtOrder order = orderRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        return modelMapper.map(order, CourtOrderResponse.class);
    }

    public List<CourtOrderResponse> fetchAllCourtOrders(){
        return orderRepo.findAll().stream()
                .map(order->modelMapper.map(order,CourtOrderResponse.class))
                .collect(Collectors.toList());
    }

    public List<CourtOrderResponse> fetchCourtOrdersByCaseId(Long caseId){
        return orderRepo.findByCaseEntityCaseId(caseId).stream()
                .map(order->modelMapper.map(order,CourtOrderResponse.class))
                .collect(Collectors.toList());
    }

    public CourtOrderResponse updateCourtOrderStatus(Long id, CourtOrder.OrderStatus status){
        CourtOrder order = orderRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        order.setStatus(status);
        CourtOrder savedOrder = orderRepo.save(order);

        auditLogService.recordSystemAction(savedOrder.getJudge().getUserId(), "COURT_ORDER_STATUS_UPDATED",
                "CourtOrder:" + id + " -> " + status);
        return modelMapper.map(savedOrder, CourtOrderResponse.class);
    }

    // --- PRIVATE HELPERS ---

    private void sendNotification(Long userId, Long entityId, String message) {
        NotificationRequest n = new NotificationRequest();
        n.setUserId(userId);
        n.setEntityId(entityId);// entity id
        n.setMessage(message);
        n.setCategory(Notification.NotificationCategory.JUDGMENT);

        notificationService.dispatchNotification(n);
    }
}