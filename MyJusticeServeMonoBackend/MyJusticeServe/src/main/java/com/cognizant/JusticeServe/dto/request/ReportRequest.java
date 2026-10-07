package com.cognizant.JusticeServe.dto.request;

import com.cognizant.JusticeServe.entity.Report;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportRequest {
    @NotNull(message = "Scope is required")
    private Report.ReportScope scope;

    @NotNull(message = "User ID is required")
    private Long requesterId;
}
