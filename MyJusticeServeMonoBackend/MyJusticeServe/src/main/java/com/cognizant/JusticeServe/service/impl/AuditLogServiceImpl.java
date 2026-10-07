package com.cognizant.JusticeServe.service.impl;

import com.cognizant.JusticeServe.dto.response.AuditLogResponse;
import com.cognizant.JusticeServe.entity.AuditLog;
import com.cognizant.JusticeServe.entity.User;
import com.cognizant.JusticeServe.exception.ResourceNotFoundException;
import com.cognizant.JusticeServe.repository.AuditLogRepository;
import com.cognizant.JusticeServe.repository.UserRepository;
import com.cognizant.JusticeServe.service.AuditLogService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepo;
    private final UserRepository userRepo;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public void recordSystemAction(Long userId, String action, String resource) {
        // Just throw the exception; GlobalExceptionHandler will catch it and return a 404
        User actingUser = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for auditing: " + userId));

        AuditLog entry = AuditLog.builder()
                .user(actingUser)
                .action(action)
                .resource(resource)
                .build();

        auditLogRepo.save(entry);
        log.debug("AUDIT | userId={} | action={} | resource={}", userId, action, resource);

    }

    @Override
    public List<AuditLogResponse> fetchAllAuditTrails() {
        return auditLogRepo.findAll()
                .stream()
                .map(log -> modelMapper.map(log, AuditLogResponse.class))
                .toList();
    }

    @Override
    public List<AuditLogResponse> fetchAuditHistoryByUser(Long userId) {
        return auditLogRepo.findByUserUserIdOrderByTimestampDesc(userId)
                .stream()
                .map(log -> modelMapper.map(log, AuditLogResponse.class))
                .toList();
    }
}