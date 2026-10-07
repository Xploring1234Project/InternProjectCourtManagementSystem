package com.cognizant.JusticeServe.entity;

import jakarta.persistence.*;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;


@Entity
@Table(name = "hearings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Hearing {

    public enum HearingStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, ADJOURNED, CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hearingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private Case caseEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "judge_id", nullable = false)
    private User judge;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime time;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private HearingStatus status;

    @PrePersist
    public void prePersist() {
        if (this.status == null) this.status = HearingStatus.SCHEDULED;
    }
}