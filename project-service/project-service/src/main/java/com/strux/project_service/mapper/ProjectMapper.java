package com.strux.project_service.mapper;

import com.strux.project_service.dto.*;
import com.strux.project_service.enums.PhaseStatus;
import com.strux.project_service.enums.ProjectStatus;
import com.strux.project_service.model.Project;
import com.strux.project_service.model.ProjectPhase;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProjectMapper {

    // ProjectResponse
    public ProjectResponse toProjectResponse(Project project) {
        if (project == null) return null;

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .type(project.getType())
                .status(project.getStatus())
                .companyId(project.getCompanyId())
                // Location eklendi
                .location(toProjectLocationResponse(project.getLocation()))
                // Dates
                .startDate(project.getStartDate())
                .plannedEndDate(project.getPlannedEndDate())
                .actualEndDate(project.getActualEndDate())
                .totalUnits(project.getTotalUnits())
                .overallProgress(project.getOverallProgress())
                .imageUrl(project.getImageUrl())
                // Phases eklendi
                .phases(project.getPhases() != null ?
                        project.getPhases().stream()
                                .map(this::toPhaseResponse)
                                .collect(Collectors.toList()) : null)
                .build();
    }

    // ProjectDetailResponse
    public ProjectDetailResponse toProjectDetailResponse(Project project) {
        if (project == null) return null;

        return ProjectDetailResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .type(project.getType())
                .status(project.getStatus())
                .companyId(project.getCompanyId())
                // Location
                .location(toProjectLocationResponse(project.getLocation()))
                // Dates
                .startDate(project.getStartDate())
                .plannedEndDate(project.getPlannedEndDate())
                .actualEndDate(project.getActualEndDate())
                .totalUnits(project.getTotalUnits())
                .overallProgress(project.getOverallProgress())
                .imageUrl(project.getImageUrl())
                // Phases
                .phases(project.getPhases() != null ?
                        project.getPhases().stream()
                                .map(this::toPhaseResponse)
                                .collect(Collectors.toList()) : null)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    // ProjectLocationResponse
    public ProjectLocationResponse toProjectLocationResponse(Project.ProjectLocation location) {
        if (location == null) return null;

        return ProjectLocationResponse.builder()
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .address(location.getAddress())
                .city(location.getCity())
                .district(location.getDistrict())
                .country(location.getCountry())
                .placeId(location.getPlaceId())
                .build();
    }

    public ProjectMapResponse toProjectMapResponse(Project project) {
        if (project == null) return null;

        ProjectBasicInfoDTO basicInfo = ProjectBasicInfoDTO.builder()
                .id(project.getId() != null ? project.getId().replaceAll("[^0-9]", "") : null)
                .name(project.getName())
                .description(project.getDescription())
                .type(project.getType() != null ? project.getType().name() : null)
                .status(project.getStatus() != null ? project.getStatus().name() : null)
                .startDate(project.getStartDate() != null ? project.getStartDate().atStartOfDay() : null)
                .estimatedEndDate(project.getEstimatedEndDate() != null ? project.getEstimatedEndDate().atStartOfDay() : null)
                .actualEndDate(project.getActualEndDate() != null ? project.getActualEndDate().atStartOfDay() : null)
                .totalBudget(project.getTotalBudget())
                .spentBudget(project.getSpentBudget())
                .completionPercentage(project.getOverallProgress() != null ? project.getOverallProgress().intValue() : 0)
                .build();

        LocationInfoDTO locationInfo = null;
        if (project.getLocation() != null) {
            locationInfo = LocationInfoDTO.builder()
                    .latitude(project.getLocation().getLatitude() != null ?
                            String.valueOf(project.getLocation().getLatitude()) : null)
                    .longitude(project.getLocation().getLongitude() != null ?
                            String.valueOf(project.getLocation().getLongitude()) : null)
                    .address(project.getLocation().getAddress())
                    .city(project.getLocation().getCity())
                    .district(project.getLocation().getDistrict())
                    .country(project.getLocation().getCountry())
                    .placeId(project.getLocation().getPlaceId())
                    .build();
        }

        return ProjectMapResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .type(project.getType())
                .status(project.getStatus())

                .latitude(project.getLocation() != null && project.getLocation().getLatitude() != null ?
                        String.valueOf(project.getLocation().getLatitude()) : null)
                .longitude(project.getLocation() != null && project.getLocation().getLongitude() != null ?
                        String.valueOf(project.getLocation().getLongitude()) : null)
                .address(project.getLocation() != null ? project.getLocation().getAddress() : null)
                .city(project.getLocation() != null ? project.getLocation().getCity() : null)

                .overallProgress(project.getOverallProgress())
                .imageUrl(project.getImageUrl())
                .statusColor(getStatusColor(project.getStatus()))
                .basicInfo(basicInfo)
                .locationInfo(locationInfo)
                .build();
    }


    // ProjectProgressResponse
    public ProjectProgressResponse toProjectProgressResponse(Project project) {
        if (project == null) return null;

        List<ProjectPhase> phases = project.getPhases();

        long completedPhases = phases != null ?
                phases.stream().filter(p -> p.getStatus() == PhaseStatus.COMPLETED).count() : 0;
        long inProgressPhases = phases != null ?
                phases.stream().filter(p -> p.getStatus() == PhaseStatus.IN_PROGRESS).count() : 0;

        return ProjectProgressResponse.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .overallProgress(project.getOverallProgress())
                .startDate(project.getStartDate())
                .plannedEndDate(project.getPlannedEndDate())
                .estimatedEndDate(calculateEstimatedEndDate(project))
                .totalPhases(phases != null ? phases.size() : 0)
                .completedPhases((int) completedPhases)
                .inProgressPhases((int) inProgressPhases)
                .phaseProgresses(phases != null ?
                        phases.stream()
                                .map(this::toPhaseProgress)
                                .collect(Collectors.toList()) : null)
                .build();
    }

    private ProjectDetailResponse.PhaseResponse toPhaseResponse(ProjectPhase phase) {
        if (phase == null) return null;

        return ProjectDetailResponse.PhaseResponse.builder()
                .id(phase.getId())
                .phaseName(phase.getPhaseName())
                .description(phase.getDescription())
                .weightPercentage(phase.getWeightPercentage())
                .currentProgress(phase.getCurrentProgress())
                .status(phase.getStatus())
                .plannedStartDate(phase.getPlannedStartDate())
                .plannedEndDate(phase.getPlannedEndDate())
                .actualStartDate(phase.getActualStartDate())
                .actualEndDate(phase.getActualEndDate())
                .orderIndex(phase.getOrderIndex())
                .build();
    }

    // PhaseProgress
    private ProjectProgressResponse.PhaseProgress toPhaseProgress(ProjectPhase phase) {
        if (phase == null) return null;

        BigDecimal contribution = phase.getWeightPercentage()
                .multiply(phase.getCurrentProgress())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        Integer daysRemaining = null;
        Boolean isDelayed = false;

        if (phase.getPlannedEndDate() != null) {
            LocalDate today = LocalDate.now();
            daysRemaining = (int) ChronoUnit.DAYS.between(today, phase.getPlannedEndDate());
            isDelayed = daysRemaining < 0 && phase.getStatus() != PhaseStatus.COMPLETED;
        }

        return ProjectProgressResponse.PhaseProgress.builder()
                .phaseId(phase.getId())
                .phaseName(phase.getPhaseName())
                .weightPercentage(phase.getWeightPercentage())
                .currentProgress(phase.getCurrentProgress())
                .contributionToOverall(contribution)
                .status(phase.getStatus())
                .daysRemaining(daysRemaining)
                .isDelayed(isDelayed)
                .build();
    }

    private String getStatusColor(ProjectStatus status) {
        if (status == null) return "#9CA3AF";

        Map<ProjectStatus, String> colorMap = new HashMap<>();
        colorMap.put(ProjectStatus.IN_PROGRESS, "#3B82F6");
        colorMap.put(ProjectStatus.ON_HOLD, "#EF4444");
        colorMap.put(ProjectStatus.COMPLETED, "#10B981");
        colorMap.put(ProjectStatus.CANCELLED, "#6B7280");
        colorMap.put(ProjectStatus.PLANNING, "#F59E0B");

        return colorMap.getOrDefault(status, "#9CA3AF");
    }

    private LocalDate calculateEstimatedEndDate(Project project) {
        if (project.getOverallProgress() == null ||
                project.getOverallProgress().compareTo(BigDecimal.ZERO) == 0 ||
                project.getStartDate() == null) {
            return project.getPlannedEndDate();
        }

        LocalDate today = LocalDate.now();
        long daysPassed = ChronoUnit.DAYS.between(project.getStartDate(), today);

        if (daysPassed <= 0) {
            return project.getPlannedEndDate();
        }

        double progressPercent = project.getOverallProgress().doubleValue();
        double estimatedTotalDays = (daysPassed * 100.0) / progressPercent;
        long remainingDays = (long) (estimatedTotalDays - daysPassed);

        return today.plusDays(remainingDays);
    }

    public List<ProjectResponse> toProjectResponseList(List<Project> projects) {
        if (projects == null) return null;
        return projects.stream()
                .map(this::toProjectResponse)
                .collect(Collectors.toList());
    }

    public List<ProjectMapResponse> toProjectMapResponseList(List<Project> projects) {
        if (projects == null) return null;
        return projects.stream()
                .map(this::toProjectMapResponse)
                .collect(Collectors.toList());
    }
}