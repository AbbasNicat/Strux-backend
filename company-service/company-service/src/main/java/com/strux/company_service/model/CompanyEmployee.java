package com.strux.company_service.model;

import com.strux.company_service.enums.EmployeeRole;
import com.strux.company_service.enums.EmployeeStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "company_employees")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyEmployee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String companyId;

    @Column(nullable = false)
    private String userId; // User Service-dən user ID

    @Column(nullable = false)
    private String position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeRole role; // ADMIN, MANAGER, SUPERVISOR, WORKER

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status; // ACTIVE, ON_LEAVE, TERMINATED

    private String department;

    private BigDecimal salary;

    private LocalDate hireDate;

    private LocalDate terminationDate;

    @Column(length = 1000)
    private String notes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
