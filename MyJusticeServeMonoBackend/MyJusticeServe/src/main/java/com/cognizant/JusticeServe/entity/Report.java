package com.cognizant.JusticeServe.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;


@Entity
@Table(name = "reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Report {

    public enum ReportScope {
        CASE, HEARING, JUDGMENT, COMPLIANCE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ReportScope scope;

    @Column(columnDefinition = "TEXT")
    private String metrics;

    private LocalDate generatedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by")
    private User generatedBy;

    @PrePersist
    public void prePersist() {
        this.generatedDate = LocalDate.now();
    }
}