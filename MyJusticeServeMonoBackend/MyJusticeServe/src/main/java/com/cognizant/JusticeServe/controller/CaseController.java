package com.cognizant.JusticeServe.controller;

import com.cognizant.JusticeServe.dto.request.CaseRequest;
import com.cognizant.JusticeServe.dto.request.DocumentRequest;
import com.cognizant.JusticeServe.dto.response.CaseResponse;
import com.cognizant.JusticeServe.dto.response.DocumentResponse;
import com.cognizant.JusticeServe.entity.Case;
import com.cognizant.JusticeServe.entity.CitizenDocument;
import com.cognizant.JusticeServe.service.CaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
@Slf4j
public class CaseController {

    private final CaseService service;

    @PostMapping("/file")
    @PreAuthorize("hasAnyRole('CITIZEN','LAWYER','CLERK','ADMIN')")
    public ResponseEntity<CaseResponse> fileNewCase(@Valid @RequestBody CaseRequest req) {
        log.info("Request to file a new case.  Title: {} ", req.getTitle());
        CaseResponse response = service.fileNewCase(req);
        log.info("Case filed successfully with ID: {}", response.getCaseId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CLERK','JUDGE','COMPLIANCE','AUDITOR')")
    public ResponseEntity<CaseResponse> getCaseById(@PathVariable Long id) {
        log.info("Fetching details for Case ID: {}", id);
        return ResponseEntity.ok(service.getCaseById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CLERK','JUDGE','COMPLIANCE','AUDITOR')")
    public ResponseEntity<List<CaseResponse>> getAllCases() {
        log.info("Fetching all system cases");
        List<CaseResponse> cases = service.getAllCases();
        log.debug("Total cases retrieved: {}", cases.size());
        return ResponseEntity.ok(cases);
    }

    @GetMapping("/citizen/{citizenId}")
    public ResponseEntity<List<CaseResponse>> getCasesByCitizen(@PathVariable Long citizenId) {
        log.info("Fetching cases for Citizen ID: {}", citizenId);
        return ResponseEntity.ok(service.getCasesByCitizenId(citizenId));
    }

    @GetMapping("/lawyer/{lawyerId}")
    public ResponseEntity<List<CaseResponse>> getCasesByLawyer(@PathVariable Long lawyerId) {
        log.info("Fetching cases assigned to Lawyer ID: {}", lawyerId);
        return ResponseEntity.ok(service.getCasesByLawyerId(lawyerId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<CaseResponse>> getCasesByStatus(@PathVariable Case.CaseStatus status) {
        log.info("Filtering cases by status: {}", status);
        return ResponseEntity.ok(service.getCasesByStatus(status));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','CLERK','JUDGE')")
    public ResponseEntity<CaseResponse> updateCaseStatus(@PathVariable Long id, @RequestParam Case.CaseStatus status) {
        log.info("Updating status of Case ID: {} to {}", id, status);
        CaseResponse response = service.updateCaseStatus(id, status);
        log.info("Status updated successfully for Case ID: {}", id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/assign-lawyer")
    @PreAuthorize("hasAnyRole('ADMIN','CLERK')")
    public ResponseEntity<CaseResponse> assignLawyerToCase(@PathVariable Long id, @RequestParam Long lawyerId) {
        log.info("Assigning Lawyer ID: {} to Case ID: {}", lawyerId, id);
        CaseResponse response = service.assignLawyerToCase(id, lawyerId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/remove-lawyer")
    @PreAuthorize("hasAnyRole('ADMIN','CLERK')")
    public ResponseEntity<CaseResponse> removeLawyerFromCase(@PathVariable Long id) {
        log.info("Request to remove lawyer from Case ID: {}", id);
        CaseResponse response = service.removeLawyerFromCase(id);
        log.info("Lawyer successfully removed from Case ID: {}", id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/assign-judge")
    @PreAuthorize("hasAnyRole('ADMIN','CLERK')")
    public ResponseEntity<CaseResponse> assignJudgeToCase(@PathVariable Long id, @RequestParam Long judgeId) {
        log.info("Assigning Judge ID: {} to Case ID: {}", judgeId, id);
        CaseResponse response = service.assignJudgeToCase(id, judgeId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{caseId}/documents/upload")
    public ResponseEntity<DocumentResponse> uploadCaseDocument(@PathVariable Long caseId, @Valid @RequestBody DocumentRequest req) {
        log.info("Uploading document for Case ID: {}", caseId);
        return ResponseEntity.ok(service.uploadCaseDocument(caseId, req));
    }

    @GetMapping("/{caseId}/documents")
    public ResponseEntity<List<DocumentResponse>> getCaseDocuments(@PathVariable Long caseId) {
        log.info("Fetching all documents for Case ID: {}", caseId);
        return ResponseEntity.ok(service.getCaseDocumentsByCaseId(caseId));
    }

    @PatchMapping("/documents/{docId}/verify")
    @PreAuthorize("hasAnyRole('ADMIN','CLERK')")
    public ResponseEntity<DocumentResponse> verifyCaseDocument(@PathVariable Long docId, @RequestParam CitizenDocument.VerificationStatus status) {
        log.info("Document verification update: Doc ID {} set to {}", docId, status);
        DocumentResponse response = service.verifyCaseDocument(docId, status);
        log.info("Verification status updated for Doc ID: {}", docId);
        return ResponseEntity.ok(response);
    }
}