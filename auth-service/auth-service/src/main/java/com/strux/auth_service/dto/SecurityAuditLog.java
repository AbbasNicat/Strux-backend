package com.strux.auth_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class SecurityAuditLog {

    private AuditEvent eventType;
    private String userId;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;
    private Map<String, Object> additionalData;
}
