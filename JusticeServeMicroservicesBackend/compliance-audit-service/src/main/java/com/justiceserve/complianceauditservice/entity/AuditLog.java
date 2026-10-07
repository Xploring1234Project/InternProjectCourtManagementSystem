package com.justiceserve.complianceauditservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long auditLogId;

    @Column(nullable=false)
    private Long userId;

    @Column(nullable=false,length=100)
    private String action;

    @Column(nullable=false,length=150)
    private String resource;

    @Column(nullable=false)
    private LocalDateTime timestamp;

    @PrePersist public void prePersist() {
        this.timestamp=LocalDateTime.now();
    }
}