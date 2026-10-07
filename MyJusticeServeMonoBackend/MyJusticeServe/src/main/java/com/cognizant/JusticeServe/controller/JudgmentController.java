package com.cognizant.JusticeServe.controller;

import com.cognizant.JusticeServe.dto.request.CourtOrderRequest;
import com.cognizant.JusticeServe.dto.request.JudgmentRequest;
import com.cognizant.JusticeServe.dto.response.CourtOrderResponse;
import com.cognizant.JusticeServe.dto.response.JudgmentResponse;
import com.cognizant.JusticeServe.entity.CourtOrder;
import com.cognizant.JusticeServe.service.JudgmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // SLF4J Import
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j // Lombok annotation to enable logging
public class JudgmentController {

    private final JudgmentService judgmentService;

    // --- JUDGMENT ENDPOINTS ---

    @PostMapping("/judgments")
    @PreAuthorize("hasAnyRole('JUDGE','ADMIN')")
    public ResponseEntity<JudgmentResponse> recordNewJudgment(@Valid @RequestBody JudgmentRequest request) {
        log.info("Request to record a new judgment for Case ID: {}", request.getCaseId());
        JudgmentResponse response = judgmentService.recordNewJudgment(request);
        log.info("Judgment record created successfully with ID: {} for Case ID: {}", response.getJudgmentId(), request.getCaseId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/judgments")
    public ResponseEntity<List<JudgmentResponse>> getAllJudgments() {
        log.info("Fetching all judgments from the system");
        return ResponseEntity.ok(judgmentService.fetchAllJudgments());
    }

    @GetMapping("/judgments/{id}")
    public ResponseEntity<JudgmentResponse> getJudgmentById(@PathVariable Long id) {
        log.info("Fetching judgment details for ID: {}", id);
        return ResponseEntity.ok(judgmentService.fetchJudgmentById(id));
    }

    @GetMapping("/judgments/case/{caseId}")
    public ResponseEntity<List<JudgmentResponse>> getJudgmentByCaseId(@PathVariable Long caseId) {
        log.info("Querying judgments for Case ID: {}", caseId);
        return ResponseEntity.ok(judgmentService.fetchJudgmentsByCaseId(caseId));
    }

    @PatchMapping("/judgments/{id}/finalize")
    @PreAuthorize("hasAnyRole('JUDGE','ADMIN')")
    public ResponseEntity<JudgmentResponse> finalizeJudgment(@PathVariable Long id){
        log.warn("FINALIZATION: Request to finalize legal judgment ID: {}", id);
        JudgmentResponse response = judgmentService.finalizeLegalJudgment(id);
        log.info("Judgment ID: {} has been successfully finalized", id);
        return ResponseEntity.ok(response);
    }

    // --- COURT ORDER ENDPOINTS ---

    @PostMapping("/court-orders")
    @PreAuthorize("hasAnyRole('JUDGE','ADMIN')")
    public ResponseEntity<CourtOrderResponse> issueCourtOrder(@Valid @RequestBody CourtOrderRequest request) {
        log.info("Request to issue a new court order for Case ID: {}",request.getCaseId());
        CourtOrderResponse response = judgmentService.issueNewCourtOrder(request);
        log.info("Court order ID: {} issued successfully", response.getOrderId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/court-orders")
    public ResponseEntity<List<CourtOrderResponse>> getAllCourtOrders() {
        log.info("Fetching all court orders");
        return ResponseEntity.ok(judgmentService.fetchAllCourtOrders());
    }

    @GetMapping("/court-orders/{id}")
    public ResponseEntity<CourtOrderResponse> getCourtOrderById(@PathVariable Long id) {
        log.info("Fetching court order details for ID: {}", id);
        return ResponseEntity.ok(judgmentService.fetchCourtOrderById(id));
    }

    @GetMapping("/court-orders/case/{caseId}")
    public ResponseEntity<List<CourtOrderResponse>> getCourtOrdersByCaseId(@PathVariable Long caseId) {
        log.info("Fetching court orders for Case ID: {}", caseId);
        return ResponseEntity.ok(judgmentService.fetchCourtOrdersByCaseId(caseId));
    }

    @PatchMapping("/court-orders/{id}/status")
    @PreAuthorize("hasAnyRole('JUDGE','ADMIN')")
    public ResponseEntity<CourtOrderResponse> updateCourtOrderStatus(@PathVariable Long id,
                                                                     @RequestParam CourtOrder.OrderStatus status) {
        log.info("Updating status of Court Order ID: {} to {}", id, status);
        CourtOrderResponse response = judgmentService.updateCourtOrderStatus(id, status);
        log.info("Court Order ID: {} status updated to {}", id, status);
        return ResponseEntity.ok(response);
    }
}