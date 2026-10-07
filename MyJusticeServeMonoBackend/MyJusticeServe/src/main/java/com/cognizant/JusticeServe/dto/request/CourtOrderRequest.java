package com.cognizant.JusticeServe.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourtOrderRequest {
    @NotNull(message = "Case ID is required")
    private Long caseId;

    @NotNull(message = "Judge ID is required")
    private Long judgeId;

    @NotBlank(message = "Order description is required")
    private String description;
}