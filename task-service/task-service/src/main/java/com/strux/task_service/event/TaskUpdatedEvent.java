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
public class TaskUpdatedEvent {
    private String taskId;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private String fieldChanged;
    private Object oldValue;
    private Object newValue;
}
