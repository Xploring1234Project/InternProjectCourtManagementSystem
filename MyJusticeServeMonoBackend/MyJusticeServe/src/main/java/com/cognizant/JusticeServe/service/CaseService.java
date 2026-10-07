package com.cognizant.JusticeServe.service;

import com.cognizant.JusticeServe.dto.request.CaseRequest;
import com.cognizant.JusticeServe.dto.request.DocumentRequest;
import com.cognizant.JusticeServe.dto.response.CaseResponse;
import com.cognizant.JusticeServe.dto.response.DocumentResponse;
import com.cognizant.JusticeServe.entity.Case;
import com.cognizant.JusticeServe.entity.CitizenDocument;

import java.util.List;

public interface CaseService {
    CaseResponse fileNewCase(CaseRequest request);
    CaseResponse getCaseById(Long id);
    List<CaseResponse> getAllCases();
    List<CaseResponse> getCasesByCitizenId(Long citizenId);
    List<CaseResponse> getCasesByLawyerId(Long lawyerId);
    List<CaseResponse> getCasesByStatus(Case.CaseStatus status);
    CaseResponse updateCaseStatus(Long id, Case.CaseStatus status);
    CaseResponse assignLawyerToCase(Long caseId, Long lawyerId);
    CaseResponse removeLawyerFromCase(Long caseId);
    CaseResponse assignJudgeToCase(Long caseId, Long judgeId);
    DocumentResponse uploadCaseDocument(Long caseId, DocumentRequest request);
    List<DocumentResponse> getCaseDocumentsByCaseId(Long caseId);
    DocumentResponse verifyCaseDocument(Long docId, CitizenDocument.VerificationStatus status);
}
