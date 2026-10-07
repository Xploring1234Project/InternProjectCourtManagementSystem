package com.justiceserve.judgmentservice.dto;

import com.justiceserve.judgmentservice.entity.Judgment;
import lombok.Data;

import java.time.LocalDate;

@Data
public class JudgmentResponse {
    private Long judgmentId;
    private Long caseId;
    private Long judgeId;
    private String summary;
    private LocalDate date;
    private Judgment.JudgmentStatus status;
    private String caseTitle;//
}
