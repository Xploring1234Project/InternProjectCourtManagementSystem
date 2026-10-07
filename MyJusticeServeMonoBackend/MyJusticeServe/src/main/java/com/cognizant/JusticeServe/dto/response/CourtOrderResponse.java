package com.cognizant.JusticeServe.dto.response;

import com.cognizant.JusticeServe.entity.CourtOrder;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CourtOrderResponse {
    private Long orderId;
    private Long caseId;
    private String caseTitle;
    private Long judgeId;
    private String judgeName;
    private String description;
    private LocalDate date;
    private CourtOrder.OrderStatus status;
}