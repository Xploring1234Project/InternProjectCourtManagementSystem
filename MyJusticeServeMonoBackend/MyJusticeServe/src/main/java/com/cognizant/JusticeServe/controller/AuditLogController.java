package com.cognizant.JusticeServe.controller;

import com.cognizant.JusticeServe.dto.response.AuditLogResponse;
import com.cognizant.JusticeServe.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Import for SLF4J
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Slf4j
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    public ResponseEntity<List<AuditLogResponse>> getSystemAuditTrail() {
        log.info("REST request to fetch all system audit trails");

        List<AuditLogResponse> logs = auditLogService.fetchAllAuditTrails();

        log.debug("Fetched {} audit log entries", logs.size());
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    public ResponseEntity<List<AuditLogResponse>> getUserSpecificAuditHistory(@PathVariable Long userId) {
        log.info("REST request to fetch audit history for User ID: {}", userId);

        List<AuditLogResponse> userLogs = auditLogService.fetchAuditHistoryByUser(userId);

        if (userLogs.isEmpty()) {
            log.warn("No audit logs found for User ID: {}", userId);
        } else {
            log.debug("Successfully retrieved {} logs for User ID: {}", userLogs.size(), userId);
        }

        return ResponseEntity.ok(userLogs);
    }
}