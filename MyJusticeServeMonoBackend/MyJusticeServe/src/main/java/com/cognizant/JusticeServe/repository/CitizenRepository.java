package com.cognizant.JusticeServe.repository;

import com.cognizant.JusticeServe.entity.Citizen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CitizenRepository extends JpaRepository<Citizen, Long> {
    Optional<Citizen> findByUserUserId(Long userId);
}
