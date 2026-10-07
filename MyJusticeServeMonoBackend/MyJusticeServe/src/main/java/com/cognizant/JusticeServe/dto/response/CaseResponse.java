package com.cognizant.JusticeServe.dto.response;

import com.cognizant.JusticeServe.entity.Case;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CaseResponse {
    private Long caseId;
    private Long citizenId;
    private String citizenName;
    private Long lawyerId;
    private String lawyerName;
    private Long judgeId;
    private String judgeName;
    private String title;
    private String description;
    private LocalDate filedDate;
    private Case.CaseStatus status;
}
