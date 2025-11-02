package com.strux.unit_service.event;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitProgressUpdatedEvent {
    private String eventType;
    private String unitId;
    private String unitNumber;
    private String companyId;
    private String projectId;
    private String ownerId;
    private Integer oldPercentage;
    private Integer newPercentage;
    private String currentPhase;
    private LocalDateTime timestamp;
}
