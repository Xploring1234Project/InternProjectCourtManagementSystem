package com.justiceserve.complianceauditservice.repository;

import com.justiceserve.complianceauditservice.entity.ComplianceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplianceRecordRepository extends JpaRepository<ComplianceRecord,Long> {
}
