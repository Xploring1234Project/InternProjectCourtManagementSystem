package com.cognizant.JusticeServe.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class HearingRequest {
    @NotNull(message = "Case ID is required")
    private Long caseId;

    @NotNull(message = "Judge ID is required")
    private Long judgeId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Time is required")
    private LocalTime time;
}