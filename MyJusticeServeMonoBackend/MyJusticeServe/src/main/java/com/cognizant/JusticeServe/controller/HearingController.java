package com.cognizant.JusticeServe.controller;

import com.cognizant.JusticeServe.dto.request.HearingRequest;
import com.cognizant.JusticeServe.dto.request.ProceedingRequest;
import com.cognizant.JusticeServe.dto.response.HearingResponse;
import com.cognizant.JusticeServe.dto.response.ProceedingResponse;
import com.cognizant.JusticeServe.entity.Hearing;
import com.cognizant.JusticeServe.service.HearingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hearings")
@RequiredArgsConstructor
@Slf4j
public class HearingController {

    private final HearingService hearingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('JUDGE','CLERK','ADMIN')")
    public ResponseEntity<HearingResponse> scheduleNewHearing(@Valid @RequestBody HearingRequest request){
        log.info("Request to schedule a new hearing for Case ID: {} on {}", request.getCaseId(), request.getDate());
        HearingResponse response = hearingService.scheduleNewHearing(request);
        log.info("Hearing scheduled successfully with ID: {}", response.getHearingId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<HearingResponse>> getAllHearings(){
        log.info("Fetching all scheduled hearings");
        return ResponseEntity.ok(hearingService.getAllHearings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HearingResponse> getHearingById(@PathVariable Long id) {
        log.info("Fetching details for Hearing ID: {}", id);
        return ResponseEntity.ok(hearingService.getHearingById(id));
    }

    @GetMapping("/case/{caseId}")
    public ResponseEntity<List<HearingResponse>> getHearingsByCase(@PathVariable Long caseId) {
        log.info("Fetching all hearings associated with Case ID: {}", caseId);
        return ResponseEntity.ok(hearingService.getHearingsByCaseId(caseId));
    }

    @GetMapping("/judge/{judgeId}")
    public ResponseEntity<List<HearingResponse>> getHearingsByJudge(@PathVariable Long judgeId) {
        log.info("Fetching hearings calendar for Judge ID: {}", judgeId);
        return ResponseEntity.ok(hearingService.getHearingsByJudgeId(judgeId));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('JUDGE','CLERK','ADMIN')")
    public ResponseEntity<HearingResponse> updateHearingStatus(@PathVariable Long id,
                                                               @RequestParam Hearing.HearingStatus status) {
        log.info("Updating status of Hearing ID: {} to {}", id, status);
        HearingResponse response = hearingService.updateHearingStatus(id, status);
        log.info("Hearing ID: {} status successfully updated to {}", id, status);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/proceedings")
    @PreAuthorize("hasAnyRole('JUDGE','CLERK','ADMIN')")
    public ResponseEntity<ProceedingResponse> addHearingProceeding(@PathVariable Long id,
                                                                   @Valid @RequestBody ProceedingRequest request) {
        log.info("Recording new proceeding entry for Hearing ID: {}", id);
        request.setHearingId(id);
        ProceedingResponse response = hearingService.recordHearingProceeding(request);
        log.debug("Proceeding record added with ID: {} for Hearing ID: {}", response.getProceedingId(), id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/proceedings")
    public ResponseEntity<List<ProceedingResponse>> getHearingProceedings(@PathVariable Long id) {
        log.info("Fetching proceeding history for Hearing ID: {}", id);
        List<ProceedingResponse> proceedings = hearingService.fetchProceedingsByHearingId(id);
        log.debug("Found {} proceeding entries for Hearing ID: {}", proceedings.size(), id);
        return ResponseEntity.ok(proceedings);
    }
}