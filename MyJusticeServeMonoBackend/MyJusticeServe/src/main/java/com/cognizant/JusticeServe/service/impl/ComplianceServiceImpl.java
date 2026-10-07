package com.cognizant.JusticeServe.service.impl;

import com.cognizant.JusticeServe.dto.request.AuditRequest;
import com.cognizant.JusticeServe.dto.request.ComplianceRecordRequest;
import com.cognizant.JusticeServe.dto.response.AuditResponse;
import com.cognizant.JusticeServe.dto.response.ComplianceRecordResponse;
import com.cognizant.JusticeServe.entity.Audit;
import com.cognizant.JusticeServe.entity.ComplianceRecord;
import com.cognizant.JusticeServe.entity.User;
import com.cognizant.JusticeServe.exception.ResourceNotFoundException;
import com.cognizant.JusticeServe.repository.*;
import com.cognizant.JusticeServe.service.ComplianceService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.cognizant.JusticeServe.entity.ComplianceRecord.ComplianceType.*;

@Service
@RequiredArgsConstructor
public class ComplianceServiceImpl implements ComplianceService {

    private final ComplianceRecordRepository complianceRepo;
    private final AuditRepository auditRepo;
    private final UserRepository userRepo;
    private final ModelMapper modelMapper;
    private final CaseRepository caseRepo;
    private final HearingRepository hearingRepo;
    private final JudgmentRepository judgmentRepo;

    @Override
    public ComplianceRecordResponse createComplianceRecord(ComplianceRecordRequest request) {
        ComplianceRecord record =  modelMapper.map(request, ComplianceRecord.class);
        ComplianceRecord savedRecord = complianceRepo.save(record);
        return modelMapper.map(savedRecord, ComplianceRecordResponse.class);
    }

    @Override
    public List<ComplianceRecordResponse> fetchAllComplianceRecords() {
        return complianceRepo.findAll().stream()
                .map(record-> modelMapper.map(record,ComplianceRecordResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public ComplianceRecordResponse fetchComplianceRecordById(Long id) {
        ComplianceRecord record = complianceRepo.findById(id).orElseThrow(() ->new ResourceNotFoundException("Compliance record not found"+id));
        return modelMapper.map(record, ComplianceRecordResponse.class);
    }

    @Override
    public ComplianceRecordResponse updateComplianceRecordDetails(Long id, ComplianceRecordRequest request) {
        ComplianceRecord record = complianceRepo.findById(id).orElseThrow(() ->new ResourceNotFoundException("Compliance record not found"+id));
        record.setResult(request.getResult());
        record.setNotes(request.getNotes());
        ComplianceRecord savedRecord = complianceRepo.save(record);
        return modelMapper.map(savedRecord, ComplianceRecordResponse.class);
    }

    @Override
    public AuditResponse initiateNewAudit(AuditRequest request) {
        User officer = userRepo.findById(request.getOfficerId()).orElseThrow(() ->new ResourceNotFoundException("User not found"+request.getOfficerId()));
        Audit audit = modelMapper.map(request,Audit.class);
        audit.setOfficer(officer);
        audit.setStatus(Audit.AuditStatus.OPEN);
        Audit savedAudit = auditRepo.save(audit);
        return modelMapper.map(savedAudit, AuditResponse.class);
    }

    @Override
    public List<AuditResponse> fetchAllAudits() {
        return auditRepo.findAll().stream()
                .map(audit -> modelMapper.map(audit,AuditResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public AuditResponse fetchAuditById(Long id) {
        Audit audit = auditRepo.findById(id).orElseThrow(() ->new ResourceNotFoundException("Audit record not found"+id));
        return modelMapper.map(audit, AuditResponse.class);
    }

    @Override
    public AuditResponse updateAuditProcessStatus(Long id, Audit.AuditStatus status) {
        Audit audit = auditRepo.findById(id).orElseThrow(() ->new ResourceNotFoundException("Audit record not found"+id));
        audit.setStatus(status);
        Audit savedAudit = auditRepo.save(audit);
        return modelMapper.map(savedAudit, AuditResponse.class);
    }


}
