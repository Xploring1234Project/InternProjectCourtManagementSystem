package com.cognizant.JusticeServe.repository;

import com.cognizant.JusticeServe.entity.Hearing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HearingRepository extends JpaRepository<Hearing,Long> {
    List<Hearing> findByCaseEntityCaseId(Long caseId);
    List<Hearing> findByJudgeUserId(Long judgeId);

    long countByStatus(Hearing.HearingStatus hearingStatus);
}

