package com.strux.project_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.strux.project_service.enums.PhaseStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// progress servisi ucun
@Entity
@Data
@Table(name = "project_phases")
public class ProjectPhase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id")
    @JsonIgnore
    private Project project;

    private String phaseName; // sirket adimini yada direkt admin belirleyecek
    private String description;

    @Column(precision = 5, scale = 2)
    private BigDecimal weightPercentage = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    private BigDecimal currentProgress = BigDecimal.ZERO; // 0-100 (isciler tamamlayanda ilerleyecek)

    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;

    @Enumerated(EnumType.STRING)
    private PhaseStatus status; // NOT_STARTED, IN_PROGRESS, COMPLETED, CANCELLED

    private Integer orderIndex; // Phase lerin sirasi

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
