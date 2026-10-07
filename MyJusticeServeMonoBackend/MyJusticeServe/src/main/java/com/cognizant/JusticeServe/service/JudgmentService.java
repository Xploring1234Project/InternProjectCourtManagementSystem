package com.cognizant.JusticeServe.service;

import com.cognizant.JusticeServe.dto.request.CourtOrderRequest;
import com.cognizant.JusticeServe.dto.request.JudgmentRequest;
import com.cognizant.JusticeServe.dto.response.CourtOrderResponse;
import com.cognizant.JusticeServe.dto.response.JudgmentResponse;
import com.cognizant.JusticeServe.entity.CourtOrder;

import java.util.List;

public interface JudgmentService {
    // Judgment Methods
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