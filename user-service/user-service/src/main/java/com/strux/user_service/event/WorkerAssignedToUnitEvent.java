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
public class WorkerAssignedToUnitEvent implements Serializable {
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

    // Assigner info
    private String assignedBy;         // UUID of the person who assigned
    private String assignerName;       // ✅ NEW: Assigner's full name

    // Optional: related project
    private String projectId;
}