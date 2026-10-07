package com.cognizant.JusticeServe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name="citizen_documents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CitizenDocument {

    public enum DocType {
        PETITION, EVIDENCE, ORDER, ID_PROOF, LEGAL_DOC
    }

    public enum VerificationStatus {
        PENDING, VERIFIED, REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="citizen_id",nullable = false)
    private Citizen citizen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private DocType docType;              // IDProof, LegalDoc

    @Column(nullable = false,length = 300)
    private String fileUri;

    private LocalDate uploadedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false,length = 15)
    private VerificationStatus verificationStatus;   // Pending, Approved, Rejected

    @PrePersist
    public void prePersist() {
        this.uploadedDate = LocalDate.now();
        if(this.verificationStatus == null) {
            this.verificationStatus = VerificationStatus.PENDING;
        }
    }
}