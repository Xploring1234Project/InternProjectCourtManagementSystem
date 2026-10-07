package com.cognizant.JusticeServe.controller;

import com.cognizant.JusticeServe.dto.request.ReportRequest;
import com.cognizant.JusticeServe.dto.response.ReportResponse;
import com.cognizant.JusticeServe.entity.Report;
import com.cognizant.JusticeServe.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // SLF4J Import
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE','AUDITOR')")
    public ResponseEntity<ReportResponse> generateReport(@Valid @RequestBody ReportRequest request) {
        log.info("Request to generate a new report. Scope: {}",request.getScope());

        ReportResponse response = reportService.initiateReportGeneration(request);

        log.info("Report generation initiated successfully. Assigned ID: {}", response.getReportId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE','AUDITOR')")
    public ResponseEntity<List<ReportResponse>> getAllReports() {
        log.info("Fetching all generated reports from the system");
        List<ReportResponse> reports = reportService.fetchAllGeneratedReports();
        log.debug("Total reports retrieved: {}", reports.size());
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE','AUDITOR')")
    public ResponseEntity<ReportResponse> getReportById(@PathVariable Long id) {
        log.info("Accessing report details for Report ID: {}", id);
        return ResponseEntity.ok(reportService.fetchReportDetailsById(id));
    }

    @GetMapping("/scope/{scope}")
    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE','AUDITOR')")
    public ResponseEntity<List<ReportResponse>> getReportsByScope(@PathVariable Report.ReportScope scope) {
        log.info("Filtering reports by scope: {}", scope);
        List<ReportResponse> filteredReports = reportService.fetchReportsByScope(scope);
        log.debug("Found {} reports for scope: {}", filteredReports.size(), scope);
        return ResponseEntity.ok(filteredReports);
    }
}