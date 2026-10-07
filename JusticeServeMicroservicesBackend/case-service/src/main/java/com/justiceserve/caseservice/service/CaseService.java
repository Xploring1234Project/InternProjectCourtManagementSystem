package com.justiceserve.caseservice.service;

import com.justiceserve.caseservice.dto.CaseRequest;
import com.justiceserve.caseservice.dto.CaseResponse;
import com.justiceserve.caseservice.dto.DocumentRequest;
import com.justiceserve.caseservice.dto.DocumentResponse;
import com.justiceserve.caseservice.entity.Case;
import com.justiceserve.caseservice.entity.CaseDocument;

import java.util.List;

public interface CaseService {
    CaseResponse fileNewCase(CaseRequest request);
    CaseResponse getCaseById(Long id);
    List<CaseResponse> getAllCases();
    List<CaseResponse> getCasesByCitizenId(Long citizenId);
    List<CaseResponse> getCasesByLawyerId(Long lawyerId);
    List<CaseResponse> getCasesByJudgeId(Long judgeId);
    List<CaseResponse> getCasesByStatus(Case.CaseStatus status);
    CaseResponse updateCaseStatus(Long id, Case.CaseStatus status);
    CaseResponse assignLawyerToCase(Long caseId, Long lawyerId);
    CaseResponse removeLawyerFromCase(Long caseId);
    CaseResponse assignJudgeToCase(Long caseId, Long judgeId);
    DocumentResponse uploadCaseDocument(Long caseId, DocumentRequest request);
    List<DocumentResponse> getCaseDocumentsByCaseId(Long caseId);
    DocumentResponse verifyCaseDocument(Long docId, CaseDocument.VerificationStatus status);
}
