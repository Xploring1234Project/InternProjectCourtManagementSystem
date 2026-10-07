package com.justiceserve.complianceauditservice.service.impl;

import com.justiceserve.complianceauditservice.dto.AuditLogResponse;
import com.justiceserve.complianceauditservice.entity.AuditLog;
import com.justiceserve.complianceauditservice.exception.ResourceNotFoundException;
import com.justiceserve.complianceauditservice.feignclient.UserFeignClient;
import com.justiceserve.complianceauditservice.repository.AuditLogRepository;
import com.justiceserve.complianceauditservice.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final UserFeignClient userClient;
    private final AuditLogRepository auditLogRepo;
    private final ModelMapper modelMapper;

    @Override
    public void log(Long userId, String action, String resource) {
        var actingUser = userClient.getUserById(userId);
        if (actingUser == null) {
            new ResourceNotFoundException("User not found for auditing: " + userId);
        }


        AuditLog entry = AuditLog.builder()
                .userId(actingUser.userId())
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
        return auditLogRepo.findByUserIdOrderByTimestampDesc(userId)
                .stream()
                .map(log -> modelMapper.map(log, AuditLogResponse.class))
                .toList();
    }
}
