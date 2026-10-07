package com.cognizant.JusticeServe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name="cases")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Case {

    public enum CaseStatus{
        FILED,UNDER_REVIEW, ACTIVE,HEARING_SCHEDULED, JUDGEMENT_PENDING,CLOSED,DISMISSED
    }

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long caseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="citizen_id",nullable = false)
    private Citizen citizen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="lawyer_id")
    private User lawyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "judge_id")
    private User judge;

    @Column(nullable = false,length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate filedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 25)
    private CaseStatus status;

    @PrePersist
    public void prePersist() {
        this.filedDate = LocalDate.now();
        if (this.status == null) this.status = CaseStatus.FILED;
    }

}