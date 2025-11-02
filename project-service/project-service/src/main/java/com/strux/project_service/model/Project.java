package com.strux.project_service.model;

import com.strux.project_service.enums.ProjectStatus;
import com.strux.project_service.enums.ProjectType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String companyId;

    private String name;

    private String description;

    @Embedded
    private ProjectLocation location;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    @Enumerated(EnumType.STRING)
    private ProjectType type;

    private LocalDate startDate;
    private LocalDate plannedEndDate;
    private LocalDate actualEndDate;

    private LocalDate estimatedEndDate;
    private Double totalBudget;
    private Double spentBudget;

    private Integer totalUnits; // toplam konut

    @Column(precision = 5, scale = 2)
    private BigDecimal overallProgress; // 0-100 arası

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<ProjectPhase> phases;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<ScheduleItem> schedule;

    private String imageUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class ProjectLocation {
        private Double latitude;
        private Double longitude;

        private String address;
        private String city;
        private String district;
        private String country;

        private String placeId; // Google Maps Place ID
    }

}
