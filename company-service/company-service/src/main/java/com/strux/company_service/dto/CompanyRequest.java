package com.strux.company_service.dto;

import com.strux.company_service.enums.CompanyType;
import com.strux.company_service.model.CompanyAddress;
import com.strux.company_service.model.CompanyFinancials;
import com.strux.company_service.model.CompanyLicense;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Tax ID is required")
    private String taxId;

    private String registrationNumber;

    @NotNull(message = "Company type is required")
    private CompanyType type;

    private String description;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone is required")
    private String phone;

    private String website;

    private CompanyAddress address;

    private Integer employeeCount;

    private CompanyFinancials financials;

    private CompanyLicense license;

    private String ownerId;
}
