package com.justiceserve.complianceauditservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogResponse {
    private Long auditLogId;
    private Long userId;
    //private String userName;
    private String action;
    private String resource;
    private LocalDateTime timestamp;
}
