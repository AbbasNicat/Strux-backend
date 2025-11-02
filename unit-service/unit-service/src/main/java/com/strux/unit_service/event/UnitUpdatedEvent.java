package com.strux.unit_service.event;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitUpdatedEvent {
    private String eventType;
    private String unitId;
    private String companyId;
    private String projectId;
    private LocalDateTime timestamp;
}
