package com.justiceserve.complianceauditservice.repository;

import com.justiceserve.complianceauditservice.entity.Audit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<Audit,Long> {
}
