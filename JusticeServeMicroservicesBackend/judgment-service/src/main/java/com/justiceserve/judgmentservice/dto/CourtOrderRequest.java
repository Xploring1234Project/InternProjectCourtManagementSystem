package com.justiceserve.judgmentservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourtOrderRequest {
    @NotNull
    private Long caseId;
    @NotNull
    private Long judgeId;
    //@NotNull
    //private Long citizenId;
    //@NotNull
    //private Long lawyerId;
    @NotNull
    private String description;
}