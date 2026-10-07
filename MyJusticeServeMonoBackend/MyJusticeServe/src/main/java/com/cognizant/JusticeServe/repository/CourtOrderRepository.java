package com.cognizant.JusticeServe.repository;

import com.cognizant.JusticeServe.entity.CourtOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourtOrderRepository extends JpaRepository<CourtOrder, Long> {
    List<CourtOrder> findByCaseEntityCaseId(Long caseId);
}
