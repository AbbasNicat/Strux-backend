package com.strux.unit_service.dto;

import com.strux.unit_service.enums.WorkItemStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkItemUpdateRequest {

    private String workName;
    private String description;
    private WorkItemStatus status;

    @Min(0) @Max(100)
    private Integer completionPercentage;

    @Min(1) @Max(100)
    private Integer weightPercentage;

    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;

    private String assignedContractorId;
    private String assignedWorkerId;
    private String taskId;
}
