package com.cognizant.JusticeServe.dto.response;

import com.cognizant.JusticeServe.entity.Report;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ReportResponse {
    private Long reportId;
    private Report.ReportScope scope;
    private String metrics;
    private LocalDate generatedDate;
    private Long generatedBy; // Mapped from User.userId
}
