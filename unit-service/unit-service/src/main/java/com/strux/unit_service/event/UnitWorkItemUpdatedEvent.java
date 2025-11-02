package com.strux.unit_service.event;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitWorkItemUpdatedEvent {
    private String eventType;
    private String workItemId;
    private String unitId;
    private String workName;
    private Integer oldPercentage;
    private Integer newPercentage;
    private String status;
    private LocalDateTime timestamp;
}
