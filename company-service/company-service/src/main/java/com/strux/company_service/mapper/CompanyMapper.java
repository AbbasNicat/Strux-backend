package com.strux.company_service.mapper;

import com.strux.company_service.dto.CompanyRequest;
import com.strux.company_service.dto.CompanyResponse;
import com.strux.company_service.model.Company;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper {

    public Company toEntity(CompanyRequest request) {
        if (request == null) {
            return null;
        }

        return Company.builder()
                .companyName(request.getCompanyName())
                .taxId(request.getTaxId())
                .registrationNumber(request.getRegistrationNumber())
                .type(request.getType())
                .description(request.getDescription())
                .email(request.getEmail())
                .phone(request.getPhone())
                .website(request.getWebsite())
                .address(request.getAddress())
                .employeeCount(request.getEmployeeCount())
                .financials(request.getFinancials())
                .license(request.getLicense())
                .ownerId(request.getOwnerId())
                .build();
    }

    public CompanyResponse toResponse(Company company) {
        if (company == null) {
            return null;
        }

        return CompanyResponse.builder()
                .id(company.getId())
                .companyName(company.getCompanyName())
                .taxId(company.getTaxId())
                .registrationNumber(company.getRegistrationNumber())
                .type(company.getType())
                .status(company.getStatus())
                .description(company.getDescription())
                .email(company.getEmail())
                .phone(company.getPhone())
                .website(company.getWebsite())
                .logoUrl(company.getLogoUrl())
                .address(company.getAddress())
                .employeeCount(company.getEmployeeCount())
                .activeProjects(company.getActiveProjects())
                .completedProjects(company.getCompletedProjects())
                .financials(company.getFinancials())
                .license(company.getLicense())
                .ownerId(company.getOwnerId())
                .isVerified(company.getIsVerified())
                .verifiedAt(company.getVerifiedAt())
                .verifiedBy(company.getVerifiedBy())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}