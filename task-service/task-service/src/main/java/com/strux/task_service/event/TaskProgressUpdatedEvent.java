package com.strux.task_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskProgressUpdatedEvent {
    private String taskId;
    private Integer oldProgress;
    private Integer newProgress;
    private LocalDateTime updatedAt;
}
