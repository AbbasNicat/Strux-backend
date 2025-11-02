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
public class TaskCompletedEvent {
    private String taskId;
    private String completedBy;
    private LocalDateTime completedAt;
}
