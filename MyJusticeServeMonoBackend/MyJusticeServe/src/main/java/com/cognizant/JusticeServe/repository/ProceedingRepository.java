package com.cognizant.JusticeServe.repository;

import com.cognizant.JusticeServe.entity.Proceeding;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProceedingRepository extends JpaRepository<Proceeding,Long> {
    List<Proceeding> findByHearingHearingId(Long hearingId);
}
