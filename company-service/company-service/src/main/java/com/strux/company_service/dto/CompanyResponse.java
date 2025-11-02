package com.strux.company_service.dto;

import com.strux.company_service.enums.CompanyStatus;
import com.strux.company_service.enums.CompanyType;
import com.strux.company_service.model.CompanyAddress;
import com.strux.company_service.model.CompanyFinancials;
import com.strux.company_service.model.CompanyLicense;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyResponse {

    private String id;
    private String companyName;
    private String taxId;
    private String registrationNumber;
    private CompanyType type;
    private CompanyStatus status;
    private String description;
    private String email;
    private String phone;
    private String website;
    private String logoUrl;
    private CompanyAddress address;
    private Integer employeeCount;
    private Integer activeProjects;
    private Integer completedProjects;
    private CompanyFinancials financials;
    private CompanyLicense license;
    private String ownerId;
    private Boolean isVerified;
    private LocalDateTime verifiedAt;
    private String verifiedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
