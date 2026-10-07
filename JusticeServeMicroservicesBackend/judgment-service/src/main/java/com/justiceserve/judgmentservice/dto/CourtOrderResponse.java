package com.justiceserve.judgmentservice.dto;

import com.justiceserve.judgmentservice.entity.CourtOrder;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CourtOrderResponse {
    private Long orderId;
    private Long caseId;
    private Long judgeId;
    private String description;
    private LocalDate date;
    private CourtOrder.OrderStatus status;
}
