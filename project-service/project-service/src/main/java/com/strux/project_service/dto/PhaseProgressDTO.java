package com.strux.project_service.dto;

import com.strux.project_service.enums.PhaseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhaseProgressDTO {
    private Long id;
    private String name;
    private Integer order;
    private Integer completionPercentage;
    private PhaseStatus status; // NOT_STARTED, IN_PROGRESS, COMPLETED
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer totalTasks;
    private Integer completedTasks;
}
