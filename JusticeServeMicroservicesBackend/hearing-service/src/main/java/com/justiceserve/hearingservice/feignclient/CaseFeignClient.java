package com.justiceserve.hearingservice.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;


@FeignClient(name = "case-service", fallback = CaseFeignClient.CaseFeignFallback.class)
public interface CaseFeignClient {


    @GetMapping("/api/cases/{id}")
    CaseResponse getCaseById(@PathVariable("id") Long id);

    @PatchMapping("/api/cases/{id}/status")
    CaseResponse updateCaseStatus(@PathVariable("id") Long id, @RequestParam("status") String status);

    record CaseResponse(Long caseId, Long citizenId, String citizenName,
                        Long lawyerId, String lawyerName, Long judgeId, String judgeName,
                        String title, String description, LocalDate filedDate,
                        String status, LocalDateTime createdAt) {}

    @org.springframework.stereotype.Component
    class CaseFeignFallback implements CaseFeignClient {
        @Override
        public CaseResponse getCaseById(Long id) {
            // Log this so you know the fallback was triggered!
            System.err.println("Fallback triggered for Case ID: " + id);
            return null;
        }

        @Override
        public CaseResponse updateCaseStatus(Long id, String status) {
            System.err.println("Fallback triggered for status update: " + id);
            return null;
        }
    }
}
