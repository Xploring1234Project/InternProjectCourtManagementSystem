package com.cognizant.JusticeServe.entity;

import com.cognizant.JusticeServe.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;



@Entity
@Table(name = "audits")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Audit {

    public enum AuditStatus{
        OPEN, CLOSED, REVIEW
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officer_id",nullable = false)
    private User officer;

    @Column(length = 200)
    private String scope;

    @Column(columnDefinition = "TEXT")
    private String findings;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private AuditStatus status;

    @PrePersist
    public void prePersist() {
        this.date = LocalDate.now();
        if (this.status == null) this.status = AuditStatus.OPEN;
    }
}