package com.justiceserve.judgmentservice.controller;

import com.justiceserve.judgmentservice.dto.CourtOrderRequest;
import com.justiceserve.judgmentservice.dto.CourtOrderResponse;
import com.justiceserve.judgmentservice.entity.CourtOrder;
import com.justiceserve.judgmentservice.service.JudgmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/court-orders")
@Slf4j
@RequiredArgsConstructor
public class CourtOrderController {

    private final JudgmentService judgmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('JUDGE','ADMIN')")
    public ResponseEntity<CourtOrderResponse> issueCourtOrder(@Valid @RequestBody CourtOrderRequest request) {
        log.info("Request to issue a new court order for Case ID: {}",request.getCaseId());
        CourtOrderResponse response = judgmentService.issueNewCourtOrder(request);
        log.info("Court order ID: {} issued successfully", response.getOrderId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CourtOrderResponse>> getAllCourtOrders() {
        log.info("Fetching all court orders");
        return ResponseEntity.ok(judgmentService.fetchAllCourtOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourtOrderResponse> getCourtOrderById(@PathVariable Long id) {
        log.info("Fetching court order details for ID: {}", id);
        return ResponseEntity.ok(judgmentService.fetchCourtOrderById(id));
    }

    @GetMapping("/case/{caseId}")
    public ResponseEntity<List<CourtOrderResponse>> getCourtOrdersByCaseId(@PathVariable Long caseId) {
        log.info("Fetching court orders for Case ID: {}", caseId);
        return ResponseEntity.ok(judgmentService.fetchCourtOrdersByCaseId(caseId));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('JUDGE','ADMIN')")
    public ResponseEntity<CourtOrderResponse> updateCourtOrderStatus(@PathVariable Long id,
                                                                     @RequestParam CourtOrder.OrderStatus status) {
        log.info("Updating status of Court Order ID: {} to {}", id, status);
        CourtOrderResponse response = judgmentService.updateCourtOrderStatus(id, status);
        log.info("Court Order ID: {} status updated to {}", id, status);
        return ResponseEntity.ok(response);
    }
}
