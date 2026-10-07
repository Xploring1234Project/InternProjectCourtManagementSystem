package com.justiceserve.complianceauditservice.service;

import com.justiceserve.complianceauditservice.dto.AuditLogResponse;

import java.util.List;

public interface AuditLogService {
    void log(Long userId, String action, String resource);
    List<AuditLogResponse> fetchAllAuditTrails();
    List<AuditLogResponse> fetchAuditHistoryByUser(Long userId);
}
