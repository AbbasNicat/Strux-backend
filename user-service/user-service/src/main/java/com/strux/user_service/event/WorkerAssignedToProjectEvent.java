package com.strux.user_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// common-events (ya da user_service içinde)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerAssignedToProjectEvent {
    private String eventId;         // UUID
    private String timestamp;       // ISO-8601
    private String companyId;       // hedef şirket
    private String projectId;
    private String userId;          // worker'ın userId (UUID-string)
    private String position;        // "Worker" varsayılan
    private String department;      // opsiyonel
    private String role;            // "WORKER" (EmployeeRole)
    private String issuedBy;        // atamayı yapan admin/manager id
}

