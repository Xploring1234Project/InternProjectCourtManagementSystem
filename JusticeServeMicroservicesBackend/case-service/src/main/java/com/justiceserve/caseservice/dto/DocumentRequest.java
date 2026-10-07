package com.justiceserve.caseservice.dto;

import com.justiceserve.caseservice.entity.CaseDocument;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DocumentRequest {
    @NotNull
    private CaseDocument.DocType docType;
    @NotBlank
    private String fileUri;
}