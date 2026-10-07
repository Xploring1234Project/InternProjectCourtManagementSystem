package com.justiceserve.complianceauditservice.service;

import com.justiceserve.complianceauditservice.dto.AuditRequest;
import com.justiceserve.complianceauditservice.dto.AuditResponse;
import com.justiceserve.complianceauditservice.dto.ComplianceRecordRequest;
import com.justiceserve.complianceauditservice.dto.ComplianceRecordResponse;
import com.justiceserve.complianceauditservice.entity.Audit;

import java.util.List;

public interface ComplianceService {

        // Compliance Records
        ComplianceRecordResponse createComplianceRecord(ComplianceRecordRequest request);
        List<ComplianceRecordResponse> fetchAllComplianceRecords();
        ComplianceRecordResponse fetchComplianceRecordById(Long id);
        ComplianceRecordResponse updateComplianceRecordDetails(Long id, ComplianceRecordRequest request);

        // Audits
        AuditResponse initiateNewAudit(AuditRequest request);
        List<AuditResponse> fetchAllAudits();
        AuditResponse fetchAuditById(Long id);
        AuditResponse updateAuditProcessStatus(Long id, Audit.AuditStatus status);
    }
