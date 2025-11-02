package com.strux.task_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDependencyChangedEvent {
    private String taskId;
    private List<String> oldDependencies;
    private List<String> newDependencies;
    private LocalDateTime updatedAt;
}