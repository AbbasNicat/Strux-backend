package com.strux.document_service.event;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProgressDocumentUploadedEvent {
    private String eventType = "PROGRESS_DOCUMENT_UPLOADED";
    private String folderId;
    private String documentId;
    private String taskId;
    private String unitId;
    private String projectId;
    private String workerId;
    private String fileName;
    private String documentType;
    private Integer completionPercentage;
    private String[] notifyUsers;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
}
