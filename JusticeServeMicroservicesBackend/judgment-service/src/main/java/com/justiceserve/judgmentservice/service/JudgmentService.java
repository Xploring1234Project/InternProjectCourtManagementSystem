package com.justiceserve.judgmentservice.service;

import com.justiceserve.judgmentservice.dto.CourtOrderRequest;
import com.justiceserve.judgmentservice.dto.CourtOrderResponse;
import com.justiceserve.judgmentservice.dto.JudgmentRequest;
import com.justiceserve.judgmentservice.dto.JudgmentResponse;
import com.justiceserve.judgmentservice.entity.CourtOrder;

import java.util.List;

public interface JudgmentService {
    JudgmentResponse recordNewJudgment(JudgmentRequest request);
    JudgmentResponse fetchJudgmentById(Long id);
    List<JudgmentResponse> fetchAllJudgments();
    List<JudgmentResponse> fetchJudgmentsByCaseId(Long caseId);
    JudgmentResponse finalizeLegalJudgment(Long id);

    // Court Order Methods
    CourtOrderResponse issueNewCourtOrder(CourtOrderRequest request);
    CourtOrderResponse fetchCourtOrderById(Long id);
    List<CourtOrderResponse> fetchAllCourtOrders();
    List<CourtOrderResponse> fetchCourtOrdersByCaseId(Long caseId);
    CourtOrderResponse updateCourtOrderStatus(Long id, CourtOrder.OrderStatus status);
}
