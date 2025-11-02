package com.strux.unit_service.event;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitReservedEvent {
    private String eventType;
    private String unitId;
    private String unitNumber;
    private String companyId;
    private String projectId;
    private String ownerId;
    private String ownerName;
    private LocalDateTime timestamp;
}
