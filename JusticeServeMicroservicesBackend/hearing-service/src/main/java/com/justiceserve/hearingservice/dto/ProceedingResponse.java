package com.justiceserve.hearingservice.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProceedingResponse {
    private Long proceedingId;
    private Long hearingId;
    private String notes;
    private LocalDate date;
    private String status;
}
