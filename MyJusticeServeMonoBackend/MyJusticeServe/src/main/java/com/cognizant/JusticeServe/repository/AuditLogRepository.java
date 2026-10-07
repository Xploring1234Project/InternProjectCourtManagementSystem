package com.cognizant.JusticeServe.repository;


import com.cognizant.JusticeServe.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserUserIdOrderByTimestampDesc(Long userId);

}
