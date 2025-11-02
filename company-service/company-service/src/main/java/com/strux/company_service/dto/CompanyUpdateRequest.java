package com.strux.company_service.dto;

import com.strux.company_service.model.CompanyAddress;
import com.strux.company_service.model.CompanyFinancials;
import com.strux.company_service.model.CompanyLicense;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyUpdateRequest {

    private String companyName;

    private String description;

    @Email(message = "Invalid email format")
    private String email;

    private String phone;

    private String website;

    private CompanyAddress address;

    private Integer employeeCount;

    private CompanyFinancials financials;

    private CompanyLicense license;
}
