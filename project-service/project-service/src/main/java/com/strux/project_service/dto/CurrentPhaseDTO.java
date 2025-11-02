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
public class CurrentPhaseDTO {
    private Long id;
    private String name;
    private String description;
    private Integer completionPercentage;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime estimatedEndDate;
    private Integer totalTasks;
    private Integer completedTasks;
}
