package com.strux.unit_service.dto;

import com.strux.unit_service.enums.WorkItemStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkItemCreateRequest {

    @NotBlank(message = "Work name is required")
    private String workName;

    private String description;

    @NotNull(message = "Status is required")
    private WorkItemStatus status;

    @NotNull(message = "Weight percentage is required")
    @Min(1) @Max(100)
    private Integer weightPercentage;

    private LocalDateTime startDate;
    private LocalDateTime dueDate;

    private String assignedContractorId;
    private String assignedWorkerId;
    private String taskId;
}
