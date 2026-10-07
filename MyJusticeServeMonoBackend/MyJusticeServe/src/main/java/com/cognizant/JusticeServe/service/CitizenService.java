package com.cognizant.JusticeServe.service;

import com.cognizant.JusticeServe.dto.request.CitizenRequest;
import com.cognizant.JusticeServe.dto.request.DocumentRequest;
import com.cognizant.JusticeServe.dto.response.CitizenResponse;
import com.cognizant.JusticeServe.dto.response.DocumentResponse;
import com.cognizant.JusticeServe.entity.CitizenDocument;

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
