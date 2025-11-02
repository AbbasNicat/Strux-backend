package com.strux.company_service.event;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@AllArgsConstructor
@RequiredArgsConstructor
public class CompanyDeletedEvent {
    String companyId;
    LocalDateTime deletedAt;
}
