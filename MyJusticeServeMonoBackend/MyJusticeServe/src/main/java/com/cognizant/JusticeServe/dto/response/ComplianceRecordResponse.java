package com.cognizant.JusticeServe.dto.response;

import com.cognizant.JusticeServe.entity.ComplianceRecord;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ComplianceRecordResponse {
    private Long complianceId;
    private Long entityId;
    private ComplianceRecord.ComplianceType type;
    private ComplianceRecord.ComplianceResult result;
    private LocalDate date;
    private String notes;
}