package com.cognizant.JusticeServe.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;


@Entity
@Table(name = "court_orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CourtOrder {

    public enum OrderStatus {
        ACTIVE, SERVED, EXPIRED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private Case caseEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "judge_id", nullable = false)
    private User judge;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrderStatus status;

    @PrePersist
    public void prePersist() {
        this.date = LocalDate.now();
        if (this.status == null) this.status = OrderStatus.ACTIVE;
    }
}
