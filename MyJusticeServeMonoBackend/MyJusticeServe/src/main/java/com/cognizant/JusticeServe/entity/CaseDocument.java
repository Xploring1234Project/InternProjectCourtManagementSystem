package com.cognizant.JusticeServe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name="case_documents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CaseDocument {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long documentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id",nullable = false)
    private Case caseEntity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private CitizenDocument.DocType docType;

    @Column(nullable = false,length = 300)
    private String fileUri;

    private LocalDate uploadedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 15)
    private CitizenDocument.VerificationStatus verificationStatus;

    @PrePersist
    public void prePersist() {
        this.uploadedDate = LocalDate.now();
        if (this.verificationStatus == null)
            this.verificationStatus = CitizenDocument.VerificationStatus.PENDING;
    }
}