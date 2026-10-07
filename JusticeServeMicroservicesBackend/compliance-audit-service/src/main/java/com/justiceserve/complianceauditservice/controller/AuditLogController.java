package com.justiceserve.complianceauditservice.controller;

import com.justiceserve.complianceauditservice.dto.AuditLogResponse;
import com.justiceserve.complianceauditservice.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Slf4j
public class AuditLogController {

    private final AuditLogService auditLogService;

    @PostMapping("/internal")
    public ResponseEntity<Void> log(@RequestParam("userId") Long userId,
                                    @RequestParam("action") String action,
                                    @RequestParam("resource") String resource) {
        log.info("Received internal audit log request: userId={}, action={}, resource={}", userId, action, resource);

        try {
            auditLogService.log(userId, action, resource);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to save audit log to database: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

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
