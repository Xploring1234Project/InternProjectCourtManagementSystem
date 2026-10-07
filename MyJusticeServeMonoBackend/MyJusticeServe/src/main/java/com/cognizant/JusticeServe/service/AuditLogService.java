package com.cognizant.JusticeServe.service;

import com.cognizant.JusticeServe.dto.response.AuditLogResponse;

import java.util.List;

public interface AuditLogService {
    void recordSystemAction(Long userId, String action, String resource);
    List<AuditLogResponse> fetchAllAuditTrails();
    List<AuditLogResponse> fetchAuditHistoryByUser(Long userId);
}
