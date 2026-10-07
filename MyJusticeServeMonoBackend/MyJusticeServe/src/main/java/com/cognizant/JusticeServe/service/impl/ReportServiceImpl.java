package com.cognizant.JusticeServe.service.impl;

import com.cognizant.JusticeServe.dto.request.ReportRequest;
import com.cognizant.JusticeServe.dto.response.ReportResponse;
import com.cognizant.JusticeServe.entity.*;
import com.cognizant.JusticeServe.exception.ResourceNotFoundException;
import com.cognizant.JusticeServe.repository.*;
import com.cognizant.JusticeServe.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepo;
    private final UserRepository userRepo;
    private final CaseRepository caseRepo;
    private final HearingRepository hearingRepo;
    private final JudgmentRepository judgmentRepo;
    private final ComplianceRecordRepository complianceRepo;
    private final ModelMapper modelMapper;

    public ReportResponse initiateReportGeneration(ReportRequest request){
        User user = userRepo.findById(request.getRequesterId()).orElseThrow(()->new ResourceNotFoundException("User Not Found"+request.getRequesterId()));

        Report report = modelMapper.map(request,Report.class);
        report.setGeneratedBy(user);

        String jsonMetrics = calculateMetricsByScope(request.getScope());
        report.setMetrics(jsonMetrics);
        Report savedReport = reportRepo.save(report);
        return modelMapper.map(savedReport,ReportResponse.class);
    }

    private String calculateMetricsByScope(Report.ReportScope scope) {
        return switch (scope) {
            case CASE -> {
                long total = caseRepo.count();
                long active = caseRepo.countByStatus(Case.CaseStatus.ACTIVE);
                long closed = caseRepo.countByStatus(Case.CaseStatus.CLOSED);
                yield String.format("{\"total\":%d,\"active\":%d,\"closed\":%d}", total, active, closed);
            }
            case HEARING -> {
                long total = hearingRepo.count();
                long completed = hearingRepo.countByStatus(Hearing.HearingStatus.COMPLETED);
                yield String.format("{\"total\":%d,\"completed\":%d}", total, completed);
            }
            case JUDGMENT -> {
                long total = judgmentRepo.count();
                long finalized = judgmentRepo.countByStatus(Judgment.JudgmentStatus.FINAL);
                yield String.format("{\"total\":%d,\"finalized\":%d}", total, finalized);
            }
            case COMPLIANCE -> {
                long total = complianceRepo.count();
                long compliant = complianceRepo.countByResult(ComplianceRecord.ComplianceResult.COMPLIANT);
                yield String.format("{\"total\":%d,\"compliant\":%d}", total, compliant);
            }
        };
    }

    @Override
    public List<ReportResponse> fetchAllGeneratedReports() {
        return reportRepo.findAll().stream()
                .map(report -> modelMapper.map(report, ReportResponse.class))
                .toList();
    }

    @Override
    public List<ReportResponse> fetchReportsByScope(Report.ReportScope scope) {
        return reportRepo.findByScope(scope).stream()
                .map(report -> modelMapper.map(report, ReportResponse.class))
                .toList();
    }

    @Override
    public ReportResponse fetchReportDetailsById(Long id) {
        Report report = reportRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with ID: " + id));
        return modelMapper.map(report, ReportResponse.class);
    }
}
