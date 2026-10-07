package com.cognizant.JusticeServe.service.impl;

import com.cognizant.JusticeServe.dto.request.CitizenRequest;
import com.cognizant.JusticeServe.dto.request.DocumentRequest;
import com.cognizant.JusticeServe.dto.response.CitizenResponse;
import com.cognizant.JusticeServe.dto.response.DocumentResponse;
import com.cognizant.JusticeServe.entity.Citizen;
import com.cognizant.JusticeServe.entity.CitizenDocument;
import com.cognizant.JusticeServe.entity.User;
import com.cognizant.JusticeServe.exception.ResourceNotFoundException;
import com.cognizant.JusticeServe.repository.CitizenDocumentRepository;
import com.cognizant.JusticeServe.repository.CitizenRepository;
import com.cognizant.JusticeServe.repository.UserRepository;
import com.cognizant.JusticeServe.service.AuditLogService;
import com.cognizant.JusticeServe.service.CitizenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CitizenServiceImpl implements CitizenService {

    private final CitizenRepository citizenRepo;
    private final CitizenDocumentRepository citizenDocRepo;
    private final UserRepository userRepo;
    private final AuditLogService auditLogService;
    private final ModelMapper modelMapper;

    @Override
    public CitizenResponse createCitizen(CitizenRequest request){
        User user = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found " + request.getUserId()));

        Citizen citizen = modelMapper.map(request, Citizen.class);
        citizen.setUser(user);
        citizen.setStatus(User.Status.ACTIVE);
        Citizen savedCitizen = citizenRepo.save(citizen);

        log.info("Citizen profile created: citizenId={}, userId={}", savedCitizen.getCitizenId(), savedCitizen.getUser().getUserId());
        auditLogService.recordSystemAction(savedCitizen.getUser().getUserId(), "CITIZEN_PROFILE_CREATED", "Citizen:" + savedCitizen.getCitizenId());

        return modelMapper.map(savedCitizen, CitizenResponse.class);
    }

    @Override
    public CitizenResponse getCitizenById(Long id) {
        Citizen citizen = citizenRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen not found: " + id));
        return modelMapper.map(citizen, CitizenResponse.class);
    }

    @Override
    public CitizenResponse getCitizenByUserId(Long userId) {
        Citizen citizen = citizenRepo.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen profile not found for user: " + userId));
        return modelMapper.map(citizen, CitizenResponse.class);
    }

    @Override
    public List<CitizenResponse> getAllCitizens() {
        return citizenRepo.findAll().stream()
                .map(citizen -> modelMapper.map(citizen, CitizenResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public CitizenResponse updateCitizen(Long id, CitizenRequest request) {
        Citizen existingCitizen = citizenRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen not Found: " + id));

        modelMapper.map(request, existingCitizen);
        Citizen updatedCitizen = citizenRepo.save(existingCitizen);
        return modelMapper.map(updatedCitizen, CitizenResponse.class);
    }

    @Override
    public DocumentResponse addUserDocument(Long citizenId, DocumentRequest request) {
        Citizen citizen = citizenRepo.findById(citizenId)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen not found: " + citizenId));

        CitizenDocument citizenDoc = modelMapper.map(request, CitizenDocument.class);
        citizenDoc.setCitizen(citizen);
        CitizenDocument savedDoc = citizenDocRepo.save(citizenDoc);

        //  Audit log for document upload
        auditLogService.recordSystemAction(
                citizen.getUser().getUserId(),
                "CITIZEN_DOCUMENT_UPLOADED",
                "Uploaded " + request.getDocType() + " for Citizen ID: " + citizenId
        );

        return modelMapper.map(savedDoc,DocumentResponse.class);
    }

    @Override
    public List<DocumentResponse> getDocumentsByCitizenId(Long citizenId) {
        Citizen citizen = citizenRepo.findById(citizenId)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen not found: " + citizenId));

        return citizenDocRepo.findByCitizenCitizenId(citizenId).stream()
                .map(doc -> modelMapper.map(doc,DocumentResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public DocumentResponse verifyUserDocument(Long docId, CitizenDocument.VerificationStatus status) {
        CitizenDocument citizenDoc = citizenDocRepo.findById(docId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + docId));

        citizenDoc.setVerificationStatus(status);
        CitizenDocument updatedDoc = citizenDocRepo.save(citizenDoc);
        return modelMapper.map(updatedDoc,DocumentResponse.class);
    }
}
