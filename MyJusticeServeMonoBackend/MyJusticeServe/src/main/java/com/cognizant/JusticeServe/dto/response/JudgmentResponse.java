package com.cognizant.JusticeServe.dto.response;

import com.cognizant.JusticeServe.entity.Judgment;
import lombok.Data;
import java.time.LocalDate;

@Data
public class JudgmentResponse {
    private Long judgmentId;
    private Long caseId;
    private String caseTitle;
    private Long judgeId;
    private String judgeName;
    private String summary;
    private LocalDate date;
    private Judgment.JudgmentStatus status;
}