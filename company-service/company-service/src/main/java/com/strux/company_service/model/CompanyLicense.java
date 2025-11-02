package com.strux.company_service.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import java.time.LocalDate;

@Embeddable
@Data
public class CompanyLicense {

    private String licenseNumber;

    private LocalDate issueDate;

    private LocalDate expiryDate;

    private String issuingAuthority;

    private String documentUrl;
}
