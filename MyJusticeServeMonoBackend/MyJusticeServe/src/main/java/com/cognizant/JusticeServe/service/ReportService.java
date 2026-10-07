package com.cognizant.JusticeServe.service;

import com.cognizant.JusticeServe.dto.request.ReportRequest;
import com.cognizant.JusticeServe.dto.response.ReportResponse;
import com.cognizant.JusticeServe.entity.Report;

import java.util.List;

public interface ReportService {
    ReportResponse initiateReportGeneration(ReportRequest request);
    List<ReportResponse> fetchAllGeneratedReports();
    List<ReportResponse> fetchReportsByScope(Report.ReportScope scope);
    ReportResponse fetchReportDetailsById(Long id);
}