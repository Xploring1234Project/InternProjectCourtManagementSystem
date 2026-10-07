package com.justiceserve.citizenservice.service.impl;

import com.justiceserve.citizenservice.dto.CitizenRequest;
import com.justiceserve.citizenservice.dto.CitizenResponse;
import com.justiceserve.citizenservice.dto.DocumentRequest;
import com.justiceserve.citizenservice.dto.DocumentResponse;
import com.justiceserve.citizenservice.entity.Citizen;
import com.justiceserve.citizenservice.entity.CitizenDocument;
import com.justiceserve.citizenservice.exception.BadRequestException;
import com.justiceserve.citizenservice.exception.ResourceNotFoundException;
import com.justiceserve.citizenservice.feignclient.AuditFeignClient;
import com.justiceserve.citizenservice.feignclient.UserFeignClient;
import com.justiceserve.citizenservice.repository.CitizenDocumentRepository;
import com.justiceserve.citizenservice.repository.CitizenRepository;
import com.justiceserve.citizenservice.service.CitizenService;
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
    private final UserFeignClient userClient;
    private final AuditFeignClient auditClient;
    private final ModelMapper modelMapper;

    @Override
    public CitizenResponse createCitizen(CitizenRequest req){
        var user = userClient.getUserById(req.getUserId());
        if (user == null || user.userId() == null)
            throw new BadRequestException("User not found in user-service: " + req.getUserId());

        if (citizenRepo.existsByUserId(req.getUserId()))
            throw new BadRequestException("Citizen profile already exists for userId: " + req.getUserId());

        Citizen citizen = modelMapper.map(req, Citizen.class);
        citizen.setUserId(user.userId());
        citizen.setStatus(Citizen.Status.valueOf(user.status()));
        Citizen savedCitizen = citizenRepo.save(citizen);

        log.info("Citizen profile created: citizenId={}, userId={}", savedCitizen.getCitizenId(), savedCitizen.getUserId());
        auditClient.log(req.getUserId(), "CITIZEN_PROFILE_CREATED", "Citizen:" + savedCitizen.getCitizenId());
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
        Citizen citizen = citizenRepo.findByUserId(userId)
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
        auditClient.log(citizen.getCitizenId(), "DOCUMENT_UPLOADED", "Citizen:" + citizenId + " Document:" + savedDoc.getDocumentId()
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
