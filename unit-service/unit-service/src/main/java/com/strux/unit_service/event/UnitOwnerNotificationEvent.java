package com.strux.unit_service.event;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitOwnerNotificationEvent {
    private String eventType;
    private String notificationType;
    private String unitId;
    private String unitNumber;
    private String ownerId;
    private String ownerName;
    private String ownerEmail;
    private Integer completionPercentage;
    private String currentPhase;
    private LocalDateTime timestamp;
}
