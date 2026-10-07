package com.cognizant.JusticeServe.service;

import com.cognizant.JusticeServe.dto.request.AuditRequest;
import com.cognizant.JusticeServe.dto.request.ComplianceRecordRequest;
import com.cognizant.JusticeServe.dto.response.AuditResponse;
import com.cognizant.JusticeServe.dto.response.ComplianceRecordResponse;
import com.cognizant.JusticeServe.entity.Audit;

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
