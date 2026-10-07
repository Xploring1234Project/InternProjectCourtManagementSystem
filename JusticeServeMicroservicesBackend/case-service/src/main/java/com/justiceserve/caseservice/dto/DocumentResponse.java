package com.justiceserve.caseservice.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DocumentResponse {
    private Long documentId;
    private String docType;
    private String fileUri;
    private LocalDate uploadedDate;
    private String verificationStatus;
}
