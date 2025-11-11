package com.strux.company_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyEmployeeResponse {
    private String userId;
    private String companyId;
    private String position;
    private String department;
    private String role;
    private LocalDate hireDate;
    private String status;
}

