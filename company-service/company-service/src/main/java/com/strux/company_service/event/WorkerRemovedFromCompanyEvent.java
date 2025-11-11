package com.strux.company_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerRemovedFromCompanyEvent {
    private String eventId;
    private String timestamp;
    private String companyId;
    private String userId;
    private String removedBy;
    private String reason;
}
