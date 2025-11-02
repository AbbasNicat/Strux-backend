package com.strux.company_service.event;

import com.strux.company_service.enums.CompanyStatus;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@RequiredArgsConstructor
public class CompanyStatusChangedEvent{
    String companyId;
    CompanyStatus oldStatus;
    CompanyStatus newStatus;
    LocalDateTime changedAt;
}

