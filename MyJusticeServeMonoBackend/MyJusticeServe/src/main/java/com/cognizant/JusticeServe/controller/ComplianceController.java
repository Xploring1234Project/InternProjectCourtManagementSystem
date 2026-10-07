package com.cognizant.JusticeServe.controller;

import com.cognizant.JusticeServe.dto.request.AuditRequest;
import com.cognizant.JusticeServe.dto.request.ComplianceRecordRequest;
import com.cognizant.JusticeServe.dto.response.AuditResponse;
import com.cognizant.JusticeServe.dto.response.ComplianceRecordResponse;
import com.cognizant.JusticeServe.entity.Audit;
import com.cognizant.JusticeServe.service.ComplianceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // SLF4J Import
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j // Lombok annotation for logging
public class ComplianceController {

    private final ComplianceService service;

    // --- COMPLIANCE RECORD ENDPOINTS ---

    @PostMapping("/compliance")
    @PreAuthorize("hasAnyRole('COMPLIANCE','ADMIN')")
    public ResponseEntity<ComplianceRecordResponse> createComplianceRecord(@Valid @RequestBody ComplianceRecordRequest request) {
        log.info("Request to create a new compliance record for entity: {}", request.getEntityId());
        ComplianceRecordResponse response = service.createComplianceRecord(request);
        log.info("Compliance record created successfully with ID: {}", response.getComplianceId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/compliance")
    @PreAuthorize("hasAnyRole('COMPLIANCE','ADMIN','AUDITOR')")
    public ResponseEntity<List<ComplianceRecordResponse>> getAllComplianceRecords() {
        log.info("Fetching all compliance records from the system");
        List<ComplianceRecordResponse> records = service.fetchAllComplianceRecords();
        log.debug("Found {} compliance records", records.size());
        return ResponseEntity.ok(records);
    }

    @GetMapping("/compliance/{id}")
    @PreAuthorize("hasAnyRole('COMPLIANCE','ADMIN','AUDITOR')")
    public ResponseEntity<ComplianceRecordResponse> getComplianceRecordById(@PathVariable Long id) {
        log.info("Accessing compliance record ID: {}", id);
        return ResponseEntity.ok(service.fetchComplianceRecordById(id));
    }

    @PatchMapping("/compliance/{id}")
    @PreAuthorize("hasAnyRole('COMPLIANCE','ADMIN')")
    public ResponseEntity<ComplianceRecordResponse> updateComplianceRecord(@PathVariable Long id,
                                                                           @Valid @RequestBody ComplianceRecordRequest request) {
        log.info("Updating details for compliance record ID: {}", id);
        ComplianceRecordResponse response = service.updateComplianceRecordDetails(id, request);
        log.info("Successfully updated compliance record ID: {}", id);
        return ResponseEntity.ok(response);
    }

    // --- AUDIT ENDPOINTS ---

    @PostMapping("/audits")
    @PreAuthorize("hasAnyRole('COMPLIANCE','AUDITOR','ADMIN')")
    public ResponseEntity<AuditResponse> initiateAudit(@Valid @RequestBody AuditRequest request) {
        log.info("Initiating a new audit process for case/entity");
        AuditResponse response = service.initiateNewAudit(request);
        log.warn("New Audit triggered! Audit ID: {}", response.getAuditId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/audits")
    @PreAuthorize("hasAnyRole('COMPLIANCE','AUDITOR','ADMIN')")
    public ResponseEntity<List<AuditResponse>> getAllAudits() {
        log.info("Retrieving all audit history");
        return ResponseEntity.ok(service.fetchAllAudits());
    }

    @GetMapping("/audits/{id}")
    @PreAuthorize("hasAnyRole('COMPLIANCE','AUDITOR','ADMIN')")
    public ResponseEntity<AuditResponse> getAuditById(@PathVariable Long id) {
        log.info("Querying audit details for Audit ID: {}", id);
        return ResponseEntity.ok(service.fetchAuditById(id));
    }

    @PatchMapping("/audits/{id}/status")
    @PreAuthorize("hasAnyRole('COMPLIANCE','ADMIN')")
    public ResponseEntity<AuditResponse> updateAuditStatus(@PathVariable Long id,
                                                           @RequestParam Audit.AuditStatus status) {
        log.info("Changing status of Audit ID: {} to {}", id, status);
        AuditResponse response = service.updateAuditProcessStatus(id, status);
        log.info("Audit ID: {} status updated successfully to {}", id, status);
        return ResponseEntity.ok(response);
    }
}