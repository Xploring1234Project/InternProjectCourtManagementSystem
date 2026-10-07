package com.justiceserve.hearingservice.feignclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "compliance-audit-service", fallback = AuditFeignClient.AuditFeignFallback.class)
public interface AuditFeignClient {

    @PostMapping("/api/audit-logs/internal")
    void log(@RequestParam("userId") Long userId,
             @RequestParam("action") String action,
             @RequestParam("resource") String resource);

    @org.springframework.stereotype.Component
    @Slf4j // Added logging to see failures
    class AuditFeignFallback implements AuditFeignClient {
        @Override
        public void log(Long userId, String action, String resource) {
            // CRITICAL CHANGE: Log the error so you aren't flying blind
            log.error("AUDIT FAILURE: Could not connect to compliance-audit-service for userId: {}. Action {} not logged.", userId, action);
        }
    }
}
