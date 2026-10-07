package com.justiceserve.complianceauditservice.service.impl;

import com.justiceserve.complianceauditservice.dto.AuditRequest;
import com.justiceserve.complianceauditservice.dto.AuditResponse;
import com.justiceserve.complianceauditservice.dto.ComplianceRecordRequest;
import com.justiceserve.complianceauditservice.dto.ComplianceRecordResponse;
import com.justiceserve.complianceauditservice.entity.Audit;
import com.justiceserve.complianceauditservice.entity.ComplianceRecord;
import com.justiceserve.complianceauditservice.exception.ResourceNotFoundException;
import com.justiceserve.complianceauditservice.feignclient.UserFeignClient;
import com.justiceserve.complianceauditservice.repository.AuditRepository;
import com.justiceserve.complianceauditservice.repository.ComplianceRecordRepository;
import com.justiceserve.complianceauditservice.service.ComplianceService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplianceServiceImpl implements ComplianceService {

    private final ComplianceRecordRepository complianceRepo;
    private final AuditRepository auditRepo;
    private final UserFeignClient userClient;
    private final ModelMapper modelMapper;

    @Override
    public ComplianceRecordResponse createComplianceRecord(ComplianceRecordRequest request) {
        ComplianceRecord record = modelMapper.map(request, ComplianceRecord.class);
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
        record.setType(request.getType());
        record.setResult(request.getResult());
        record.setNotes(request.getNotes());
        ComplianceRecord savedRecord = complianceRepo.save(record);
        return modelMapper.map(savedRecord, ComplianceRecordResponse.class);
    }

    @Override
    public AuditResponse initiateNewAudit(AuditRequest request) {
        var user = userClient.getUserById(request.getOfficerId());
        if (user == null) {
            throw new ResourceNotFoundException("User not found with ID: " + request.getOfficerId());
        }
        Audit audit = modelMapper.map(request, Audit.class);
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
