package com.justiceserve.complianceauditservice.dto;

import com.justiceserve.complianceauditservice.entity.ComplianceRecord;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ComplianceRecordRequest {
    @NotNull(message = "Entity ID is required")
    private Long entityId;

    @NotNull(message = "Compliance type is required")
    private ComplianceRecord.ComplianceType type;

    @NotNull(message = "Compliance result is required")
    private ComplianceRecord.ComplianceResult result;

    private String notes;
}
