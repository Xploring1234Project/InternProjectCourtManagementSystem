package com.cognizant.JusticeServe.repository;

import com.cognizant.JusticeServe.entity.ComplianceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplianceRecordRepository extends JpaRepository<ComplianceRecord, Long> {

    long countByResult(ComplianceRecord.ComplianceResult complianceResult);
}

