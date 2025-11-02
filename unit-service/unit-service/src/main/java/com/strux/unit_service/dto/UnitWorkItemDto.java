package com.strux.unit_service.dto;

import com.strux.unit_service.enums.WorkItemStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitWorkItemDto {

    private String id;
    private String unitId;
    private String workName;
    private String description;
    private WorkItemStatus status;
    private Integer completionPercentage;
    private Integer weightPercentage;
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;
    private String assignedContractorId;
    private String assignedWorkerId;
    private String taskId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
