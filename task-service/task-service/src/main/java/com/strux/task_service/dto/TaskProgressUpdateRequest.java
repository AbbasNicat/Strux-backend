package com.strux.task_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskProgressUpdateRequest {

    private Integer estimatedHours;
    private Integer actualHours;
    private Integer progressPercentage;
}
