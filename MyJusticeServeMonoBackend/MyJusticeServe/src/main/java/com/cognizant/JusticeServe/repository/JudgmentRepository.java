package com.cognizant.JusticeServe.repository;

import com.cognizant.JusticeServe.entity.Judgment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JudgmentRepository extends JpaRepository<Judgment, Long> {
    List<Judgment> findByCaseEntityCaseId(Long caseId);

    long countByStatus(Judgment.JudgmentStatus judgmentStatus);
}

