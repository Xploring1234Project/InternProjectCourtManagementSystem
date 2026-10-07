package com.cognizant.JusticeServe.dto.response;

import com.cognizant.JusticeServe.entity.Audit;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AuditResponse {
    private Long auditId;
    private Long officerId;
    private String officerName;
    private String scope;
    private String findings;
    private LocalDate date;
    private Audit.AuditStatus status;
}