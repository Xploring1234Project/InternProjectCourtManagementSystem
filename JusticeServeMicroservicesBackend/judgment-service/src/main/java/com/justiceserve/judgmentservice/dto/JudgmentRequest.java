package com.justiceserve.judgmentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JudgmentRequest {
    @NotNull
    private Long caseId;
    @NotNull
    private Long judgeId;
    //private Long citizenId;
    //private Long lawyerId;
    //private String caseTitle;
    @NotBlank
    private String summary;
}
