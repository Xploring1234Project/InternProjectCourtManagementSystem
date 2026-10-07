package com.cognizant.JusticeServe.dto.request;

import com.cognizant.JusticeServe.entity.CitizenDocument;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DocumentRequest {
    @NotNull(message = "Document type is required")
    private CitizenDocument.DocType docType;

    @NotBlank(message = "File URI is required")
    private String fileUri;
}