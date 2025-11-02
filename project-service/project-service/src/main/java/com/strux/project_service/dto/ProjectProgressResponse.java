package com.strux.project_service.dto;

import com.strux.project_service.enums.PhaseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectProgressResponse {

    private String projectId;
    private String projectName;

    private BigDecimal overallProgress;

    private LocalDate startDate;
    private LocalDate plannedEndDate;
    private LocalDate estimatedEndDate; // Hesaplanmış

    private Integer totalPhases;
    private Integer completedPhases;
    private Integer inProgressPhases;

    private List<PhaseProgress> phaseProgresses;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhaseProgress {
        private Long phaseId;
        private String phaseName;
        private BigDecimal weightPercentage;
        private BigDecimal currentProgress;
        private BigDecimal contributionToOverall;
        private PhaseStatus status;
        private Integer daysRemaining;
        private Boolean isDelayed;
    }
}
