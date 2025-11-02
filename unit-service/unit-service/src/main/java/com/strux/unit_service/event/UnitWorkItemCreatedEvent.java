package com.strux.unit_service.event;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitWorkItemCreatedEvent {
    private String eventType;
    private String workItemId;
    private String unitId;
    private String workName;
    private String companyId;
    private String projectId;
    private LocalDateTime timestamp;
}
