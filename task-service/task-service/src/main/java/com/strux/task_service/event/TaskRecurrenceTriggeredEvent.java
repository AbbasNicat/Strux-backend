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
public class TaskRecurrenceTriggeredEvent {
    private String originalTaskId;
    private String newTaskId;
    private String recurrencePattern;
    private LocalDateTime triggeredAt;
}
