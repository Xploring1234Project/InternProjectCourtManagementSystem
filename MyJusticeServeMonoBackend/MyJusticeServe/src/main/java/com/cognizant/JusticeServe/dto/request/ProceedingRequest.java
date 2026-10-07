package com.cognizant.JusticeServe.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProceedingRequest {
    @NotNull(message = "Hearing ID is required")
    private Long hearingId;

    @NotBlank(message = "Notes cannot be empty")
    private String notes;

    @NotBlank(message = "Status is required")
    private String status;
}