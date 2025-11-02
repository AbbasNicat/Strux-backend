package com.strux.unit_service.event;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitStatusChangedEvent {
    private String eventType;
    private String unitId;
    private String unitNumber;
    private String companyId;
    private String projectId;
    private String oldStatus;
    private String newStatus;
    private LocalDateTime timestamp;
}
