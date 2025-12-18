package com.strux.document_service.event;
import lombok.*;
import java.time.LocalDateTime;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentApprovedEvent {
    private String eventType = "DOCUMENT_APPROVED";
    private String documentId;
    private String folderId;
    private String taskId;
    private String approvedBy;
    private String approvalStatus;
    private String comments;
    private Boolean updateTaskProgress;
    private LocalDateTime timestamp;
}
