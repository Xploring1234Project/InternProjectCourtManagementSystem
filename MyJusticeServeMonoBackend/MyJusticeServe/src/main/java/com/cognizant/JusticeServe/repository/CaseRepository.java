package com.cognizant.JusticeServe.repository;

import com.cognizant.JusticeServe.entity.Case;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseRepository extends JpaRepository<Case,Long> {
    List<Case> findByCitizenCitizenId(Long citizenId);
    List<Case> findByLawyerUserId(Long lawyerId);
    List<Case> findByStatus(Case.CaseStatus status);

    long countByStatus(Case.CaseStatus caseStatus);
}

