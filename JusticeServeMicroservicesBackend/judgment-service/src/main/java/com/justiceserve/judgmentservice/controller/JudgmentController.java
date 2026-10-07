package com.justiceserve.judgmentservice.controller;

import com.justiceserve.judgmentservice.dto.JudgmentRequest;
import com.justiceserve.judgmentservice.dto.JudgmentResponse;
import com.justiceserve.judgmentservice.service.JudgmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/judgments")
@RequiredArgsConstructor
@Slf4j // Lombok annotation to enable logging
public class JudgmentController {

    private final JudgmentService judgmentService;

    // --- JUDGMENT ENDPOINTS ---

    @PostMapping
    @PreAuthorize("hasAnyRole('JUDGE','ADMIN')")
    public ResponseEntity<JudgmentResponse> recordNewJudgment(@Valid @RequestBody JudgmentRequest request) {
        log.info("Request to record a new judgment for Case ID: {}", request.getCaseId());
        JudgmentResponse response = judgmentService.recordNewJudgment(request);
        log.info("Judgment record created successfully with ID: {} for Case ID: {}", response.getJudgmentId(), request.getCaseId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<JudgmentResponse>> getAllJudgments() {
        log.info("Fetching all judgments from the system");
        return ResponseEntity.ok(judgmentService.fetchAllJudgments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JudgmentResponse> getJudgmentById(@PathVariable Long id) {
        log.info("Fetching judgment details for ID: {}", id);
        return ResponseEntity.ok(judgmentService.fetchJudgmentById(id));
    }

    @GetMapping("/case/{caseId}")
    public ResponseEntity<List<JudgmentResponse>> getJudgmentByCaseId(@PathVariable Long caseId) {
        log.info("Querying judgments for Case ID: {}", caseId);
        return ResponseEntity.ok(judgmentService.fetchJudgmentsByCaseId(caseId));
    }

    @PatchMapping("/{id}/finalize")
    @PreAuthorize("hasAnyRole('JUDGE','ADMIN')")
    public ResponseEntity<JudgmentResponse> finalizeJudgment(@PathVariable Long id) {
        log.warn("FINALIZATION: Request to finalize legal judgment ID: {}", id);
        JudgmentResponse response = judgmentService.finalizeLegalJudgment(id);
        log.info("Judgment ID: {} has been successfully finalized", id);
        return ResponseEntity.ok(response);
    }
}
