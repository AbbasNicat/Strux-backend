package com.strux.user_service.service;

import com.strux.user_service.enums.AuditEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class AuditLogService {

    public void logUserEvent(AuditEvent event, String userId, String performedBy, String details) {
        log.info("AUDIT: Event={}, UserId={}, PerformedBy={}, Details={}",
                event, userId, performedBy, details);
    }
}
