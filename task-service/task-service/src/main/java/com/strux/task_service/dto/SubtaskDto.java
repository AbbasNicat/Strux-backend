package com.strux.task_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubtaskDto {

    private String parentTaskId;
    private List<TaskDto> subtasks;
    private Integer totalSubtask;
    private Integer completedSubtasks;
}
