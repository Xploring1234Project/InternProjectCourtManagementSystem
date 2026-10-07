package com.cognizant.JusticeServe.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuditRequest {
    @NotNull(message = "Officer ID is required")
    private Long officerId;
    @NotBlank(message = "Scope cannot be blank")
    private String scope;
    private String findings;
}
