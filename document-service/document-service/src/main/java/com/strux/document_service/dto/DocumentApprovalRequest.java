package com.strux.document_service.dto;

import com.strux.document_service.enums.DocumentApprovalStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentApprovalRequest {
    private DocumentApprovalStatus status;
    private String approverComments;
    private String approvedBy;
    private String folderId;
    private Integer qualityRating;  // 1-5 ulduz
    private Boolean updateTaskProgress;
}
