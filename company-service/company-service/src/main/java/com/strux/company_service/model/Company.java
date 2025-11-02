package com.strux.company_service.model;

import com.strux.company_service.enums.CompanyStatus;
import com.strux.company_service.enums.CompanyType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "companies")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String companyName;

    @Column(nullable = false, unique = true)
    private String taxId; // VOEN

    @Column(unique = true)
    private String registrationNumber; // Qeydiyyat nömrəsi

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanyType type; // CONTRACTOR, DEVELOPER, SUPPLIER, CONSULTANT

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanyStatus status; // ACTIVE, SUSPENDED, INACTIVE, PENDING_VERIFICATION

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    private String website;

    private String logoUrl;

    @Embedded
    private CompanyAddress address;

    private Integer employeeCount;

    private Integer activeProjects;

    private Integer completedProjects;

    @Embedded
    private CompanyFinancials financials;

    @Embedded
    private CompanyLicense license;

    private String ownerId; // User Service-dən owner ID

    private Boolean isVerified;

    private LocalDateTime verifiedAt;

    private String verifiedBy; // System admin ID

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}
