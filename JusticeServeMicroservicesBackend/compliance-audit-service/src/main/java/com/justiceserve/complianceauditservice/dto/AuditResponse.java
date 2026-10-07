package com.justiceserve.complianceauditservice.dto;

import com.justiceserve.complianceauditservice.entity.Audit;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AuditResponse {
    private Long auditId;
    private Long officerId;
  //private String officerName;
    private String scope;
    private String findings;
    private LocalDate date;
    private Audit.AuditStatus status;
}
