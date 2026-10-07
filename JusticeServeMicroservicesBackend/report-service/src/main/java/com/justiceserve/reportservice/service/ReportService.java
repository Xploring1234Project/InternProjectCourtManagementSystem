package com.justiceserve.reportservice.service;

import com.justiceserve.reportservice.entity.Report;
import com.justiceserve.reportservice.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;

    public Report generateReport(Map<String, Object> request) {
        // Business logic: Processing the raw Map into an Entity
        Report report = Report.builder()
                .scope(Report.ReportScope.valueOf(request.get("scope").toString()))
                .metrics(request.getOrDefault("metrics", "{}").toString())
                .generatedBy(request.get("generatedBy") != null
                        ? Long.valueOf(request.get("generatedBy").toString())
                        : null)
                .build();

        return reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Report> getReportsByScope(Report.ReportScope scope) {
        return reportRepository.findByScope(scope);
    }

    @Transactional(readOnly = true)
    public Report getReportById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));
    }
}
