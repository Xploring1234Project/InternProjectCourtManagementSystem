package com.cognizant.JusticeServe.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;


@Entity
@Table(name = "judgments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Judgment {

    public enum JudgmentStatus {
        DRAFT, FINAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long judgmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private Case caseEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "judge_id", nullable = false)
    private User judge;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private JudgmentStatus status;

    @PrePersist
    public void prePersist() {
        this.date = LocalDate.now();
        if (this.status == null) this.status = JudgmentStatus.DRAFT;
    }
}
