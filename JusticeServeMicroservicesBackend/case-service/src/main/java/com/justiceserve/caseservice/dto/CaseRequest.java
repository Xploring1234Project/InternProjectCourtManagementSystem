package com.justiceserve.caseservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaseRequest {
    @NotNull
    private Long citizenId;
    private Long lawyerId;
    @NotBlank
    private String title;
    @NotBlank
    private String description;
}