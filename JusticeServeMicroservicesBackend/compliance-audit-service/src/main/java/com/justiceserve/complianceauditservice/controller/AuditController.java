package com.justiceserve.complianceauditservice.controller;

import com.justiceserve.complianceauditservice.dto.AuditRequest;
import com.justiceserve.complianceauditservice.dto.AuditResponse;
import com.justiceserve.complianceauditservice.entity.Audit;
import com.justiceserve.complianceauditservice.service.ComplianceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audits")
@RequiredArgsConstructor
@Slf4j
public class AuditController {
    private final ComplianceService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPLIANCE','AUDITOR','ADMIN')")
    public ResponseEntity<AuditResponse> initiateAudit(@Valid @RequestBody AuditRequest request) {
        log.info("Initiating a new audit process for case/entity");
        AuditResponse response = service.initiateNewAudit(request);
        log.warn("New Audit triggered! Audit ID: {}", response.getAuditId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('COMPLIANCE','AUDITOR','ADMIN')")
    public ResponseEntity<List<AuditResponse>> getAllAudits() {
        log.info("Retrieving all audit history");
        return ResponseEntity.ok(service.fetchAllAudits());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPLIANCE','AUDITOR','ADMIN')")
    public ResponseEntity<AuditResponse> getAuditById(@PathVariable Long id) {
        log.info("Querying audit details for Audit ID: {}", id);
        return ResponseEntity.ok(service.fetchAuditById(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('COMPLIANCE','ADMIN')")
    public ResponseEntity<AuditResponse> updateAuditStatus(@PathVariable Long id,
                                                           @RequestParam Audit.AuditStatus status) {
        log.info("Changing status of Audit ID: {} to {}", id, status);
        AuditResponse response = service.updateAuditProcessStatus(id, status);
        log.info("Audit ID: {} status updated successfully to {}", id, status);
        return ResponseEntity.ok(response);
    }
}
