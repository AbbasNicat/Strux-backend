package com.strux.auth_service.service;

import com.strux.auth_service.dto.AuditEvent;
import com.strux.auth_service.dto.SecurityAuditLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void logSecurityEvent(AuditEvent eventType, String userId, String ipAddress,
                                 String userAgent) {
        logSecurityEvent(eventType, userId, ipAddress, userAgent, Map.of());
    }

    public void logSecurityEvent(AuditEvent eventType, String userId, String ipAddress,
                                 String userAgent, Map<String, Object> additionalData) {
        try {
            SecurityAuditLog auditLog = SecurityAuditLog.builder()
                    .eventType(eventType)
                    .userId(userId)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .timestamp(LocalDateTime.now())
                    .additionalData(additionalData)
                    .build();

            kafkaTemplate.send("security-audit-logs", auditLog);

            // Also log to file for immediate visibility
            log.info("SECURITY_AUDIT: {} - User: {}, IP: {}",
                    eventType, userId, ipAddress);
        } catch (Exception e) {
            log.error("Audit log failed: {}", e.getMessage(), e);
        }
    }
}
