package com.strux.company_service.model;

import com.strux.company_service.enums.EquipmentStatus;
import com.strux.company_service.enums.EquipmentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "company_equipment")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String companyId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private EquipmentType type;

    private String model;

    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentStatus status; // AVAILABLE, IN_USE, MAINTENANCE, BROKEN

    private String currentProjectId; // Hazırda hansı proyektdə istifadə olunur

    private BigDecimal purchasePrice;

    private LocalDate purchaseDate;

    private LocalDate lastMaintenanceDate;

    private LocalDate nextMaintenanceDate;

    @Column(length = 1000)
    private String notes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

