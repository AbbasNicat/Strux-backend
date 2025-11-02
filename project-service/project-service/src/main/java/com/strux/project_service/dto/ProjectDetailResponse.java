package com.strux.project_service.dto;

import com.strux.project_service.enums.PhaseStatus;
import com.strux.project_service.enums.ProjectStatus;
import com.strux.project_service.enums.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDetailResponse {

    private String id;
    private String name;
    private String description;
    private ProjectType type;
    private ProjectStatus status;

    private String companyId;
    private String companyName;

    // Full location
    private ProjectLocationResponse location;

    private LocalDate startDate;
    private LocalDate plannedEndDate;
    private LocalDate actualEndDate;

    private Integer totalUnits;
    private BigDecimal overallProgress;

    private String imageUrl;

    private List<PhaseResponse> phases;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhaseResponse {
        private Long id;
        private String phaseName;
        private String description;
        private BigDecimal weightPercentage;
        private BigDecimal currentProgress;
        private PhaseStatus status;
        private LocalDate plannedStartDate;
        private LocalDate plannedEndDate;
        private LocalDate actualStartDate;
        private LocalDate actualEndDate;
        private Integer orderIndex;
    }
}
