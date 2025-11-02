package com.strux.project_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectBasicInfoDTO {
    private String id;
    private String name;
    private String description;
    private String type; // RESIDENTIAL, COMMERCIAL, INFRASTRUCTURE
    private String status; // PLANNING, IN_PROGRESS, COMPLETED, ON_HOLD
    private LocalDateTime endDate;
    private LocalDateTime startDate;
    private LocalDateTime estimatedEndDate;
    private LocalDateTime actualEndDate;
    private Double totalBudget;
    private Double spentBudget;
    private Integer completionPercentage;
}
