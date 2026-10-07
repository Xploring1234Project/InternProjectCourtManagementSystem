package com.cognizant.JusticeServe.dto.response;

import com.cognizant.JusticeServe.entity.CitizenDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private Long documentId;
    private CitizenDocument.DocType docType;
    private String fileUri;
    private LocalDate uploadedDate;
    private CitizenDocument.VerificationStatus verificationStatus;
}