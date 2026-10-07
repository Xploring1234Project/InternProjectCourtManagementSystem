package com.cognizant.JusticeServe.controller;

import com.cognizant.JusticeServe.dto.request.CitizenRequest;
import com.cognizant.JusticeServe.dto.request.DocumentRequest;
import com.cognizant.JusticeServe.dto.response.CitizenResponse;
import com.cognizant.JusticeServe.dto.response.DocumentResponse;
import com.cognizant.JusticeServe.entity.Citizen;
import com.cognizant.JusticeServe.entity.CitizenDocument;
import com.cognizant.JusticeServe.service.CitizenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/citizens")
@RequiredArgsConstructor
@Slf4j
public class CitizenController {

    private final CitizenService citizenService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CITIZEN','ADMIN','CLERK')")
    public ResponseEntity<CitizenResponse> createCitizen(@Valid @RequestBody CitizenRequest request){
        log.info("Request to create a new citizen profile for User ID: {}", request.getUserId());
        CitizenResponse response = citizenService.createCitizen(request);
        log.info("Citizen profile created successfully with ID: {}", response.getCitizenId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CLERK')")
    public ResponseEntity<List<CitizenResponse>> getAllCitizen(){
        log.info("Request to fetch all citizen records");
        List<CitizenResponse> citizens = citizenService.getAllCitizens();
        log.debug("Retrieved {} citizen records", citizens.size());
        return ResponseEntity.ok(citizens);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CitizenResponse> getCitizenById(@PathVariable Long id){
        log.info("Fetching citizen details for ID: {}", id);
        return ResponseEntity.ok(citizenService.getCitizenById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<CitizenResponse> getCitizenByUserId(@PathVariable Long userId){
        log.info("Fetching citizen profile associated with User ID: {}", userId);
        return ResponseEntity.ok(citizenService.getCitizenByUserId(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CitizenResponse> updateCitizen(@PathVariable Long id,@Valid @RequestBody CitizenRequest request ){
        log.info("Updating citizen profile ID: {}", id);
        CitizenResponse response = citizenService.updateCitizen(id, request);
        log.info("Citizen profile ID: {} updated successfully", id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{citizenId}/documents")
    public ResponseEntity<DocumentResponse> addUserDocument(@PathVariable Long citizenId,@Valid @RequestBody DocumentRequest request){
        log.info("Adding document for Citizen ID: {}", citizenId);
        return ResponseEntity.ok(citizenService.addUserDocument(citizenId, request));
    }

    @GetMapping("/{citizenId}/documents")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByCitizenId(@PathVariable Long citizenId){
        log.info("Fetching all documents for Citizen ID: {}", citizenId);
        return ResponseEntity.ok(citizenService.getDocumentsByCitizenId(citizenId));
    }

    @PatchMapping("/documents/{docId}/verify")
    @PreAuthorize("hasAnyRole('ADMIN','CLERK')")
    public ResponseEntity<DocumentResponse> verifyUserDocument(@PathVariable Long docId, @RequestParam CitizenDocument.VerificationStatus status){
        log.info("Verification update for Document ID: {}. New Status: {}", docId, status);
        DocumentResponse response = citizenService.verifyUserDocument(docId, status);
        log.info("Document ID: {} verification status updated to {}", docId, status);
        return ResponseEntity.ok(response);
    }
}