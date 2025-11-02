package com.strux.company_service.event;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@RequiredArgsConstructor
public class CompanyCreatedEvent {
    String id;
    String companyName;
    String taxId; // VOEN
    LocalDateTime createdAt;
}
