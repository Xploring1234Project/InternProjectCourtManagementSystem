package com.justiceserve.reportservice.controller;

import com.justiceserve.reportservice.entity.Report;
import com.justiceserve.reportservice.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE','AUDITOR')")
    public ResponseEntity<Report> generate(@RequestBody Map<String, Object> req) {
        Report generatedReport = reportService.generateReport(req);
        return ResponseEntity.ok(generatedReport);
    }

    @GetMapping
    public ResponseEntity<List<Report>> getAll() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/scope/{scope}")
    public ResponseEntity<List<Report>> getByScope(@PathVariable Report.ReportScope scope) {
        return ResponseEntity.ok(reportService.getReportsByScope(scope));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Report> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }
}
