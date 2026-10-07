package com.justiceserve.complianceauditservice.controller;

import com.justiceserve.complianceauditservice.dto.ComplianceRecordRequest;
import com.justiceserve.complianceauditservice.dto.ComplianceRecordResponse;
import com.justiceserve.complianceauditservice.service.ComplianceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compliance")
@RequiredArgsConstructor
@Slf4j // Lombok annotation for logging
public class ComplianceController {

    private final ComplianceService service;

    // --- COMPLIANCE RECORD ENDPOINTS ---

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPLIANCE','ADMIN')")
    public ResponseEntity<ComplianceRecordResponse> createComplianceRecord(@Valid @RequestBody ComplianceRecordRequest request) {
        log.info("Request to create a new compliance record for entity: {}", request.getEntityId());
        ComplianceRecordResponse response = service.createComplianceRecord(request);
        log.info("Compliance record created successfully with ID: {}", response.getComplianceId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('COMPLIANCE','ADMIN','AUDITOR')")
    public ResponseEntity<List<ComplianceRecordResponse>> getAllComplianceRecords() {
        log.info("Fetching all compliance records from the system");
        List<ComplianceRecordResponse> records = service.fetchAllComplianceRecords();
        log.debug("Found {} compliance records", records.size());
        return ResponseEntity.ok(records);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPLIANCE','ADMIN','AUDITOR')")
    public ResponseEntity<ComplianceRecordResponse> getComplianceRecordById(@PathVariable Long id) {
        log.info("Accessing compliance record ID: {}", id);
        return ResponseEntity.ok(service.fetchComplianceRecordById(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPLIANCE','ADMIN')")
    public ResponseEntity<ComplianceRecordResponse> updateComplianceRecord(@PathVariable Long id,
                                                                           @Valid @RequestBody ComplianceRecordRequest request) {
        log.info("Updating details for compliance record ID: {}", id);
        ComplianceRecordResponse response = service.updateComplianceRecordDetails(id, request);
        log.info("Successfully updated compliance record ID: {}", id);
        return ResponseEntity.ok(response);
    }


}
