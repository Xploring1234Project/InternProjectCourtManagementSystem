package com.justiceserve.citizenservice.service;

import com.justiceserve.citizenservice.dto.CitizenRequest;
import com.justiceserve.citizenservice.dto.CitizenResponse;
import com.justiceserve.citizenservice.dto.DocumentRequest;
import com.justiceserve.citizenservice.dto.DocumentResponse;
import com.justiceserve.citizenservice.entity.CitizenDocument;

import java.util.List;

public interface CitizenService {
    CitizenResponse createCitizen(CitizenRequest request);
    CitizenResponse getCitizenById(Long id);
    CitizenResponse getCitizenByUserId(Long userId);
    List<CitizenResponse> getAllCitizens();
    CitizenResponse updateCitizen(Long id, CitizenRequest request);
    DocumentResponse addUserDocument(Long citizenId, DocumentRequest request);
    List<DocumentResponse> getDocumentsByCitizenId(Long citizenId);
    DocumentResponse verifyUserDocument(Long docId, CitizenDocument.VerificationStatus status);
}