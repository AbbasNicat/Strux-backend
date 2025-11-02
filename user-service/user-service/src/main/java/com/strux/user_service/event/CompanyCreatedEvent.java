package com.strux.user_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

public record CompanyCreatedEvent(
        String companyId,
        String companyName,
        String taxId,
        LocalDateTime createdAt
) {}
