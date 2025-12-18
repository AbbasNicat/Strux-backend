package com.strux.user_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerRemovedFromUnitEvent implements Serializable {
    private String eventId;
    private String timestamp;

    // Worker info
    private String workerId;
    private String workerName;        // ✅ NEW: Worker's full name

    // Unit info
    private String unitId;
    private String unitName;

    // Company info
    private String companyId;

    // Remover info
    private String removedBy;          // UUID of the person who removed
    private String removerName;        // ✅ NEW: Remover's full name

    // Reason for removal
    private String reason;
}