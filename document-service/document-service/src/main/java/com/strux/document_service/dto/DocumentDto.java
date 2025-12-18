package com.strux.document_service.dto;

import com.strux.document_service.enums.*;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {

    private String id;
    private String fileName;
    private String originalFileName;
    private Long fileSize;
    private String taskId;
    private String mimeType;
    private String fileExtension;

    private String folderId;

    private DocumentType documentType;
    private DocumentCategory category;
    private String description;
    private Set<String> tags;

    private EntityType entityType;
    private String entityId;

    private String uploadedBy;
    private String companyId;
    private LocalDateTime uploadedAt;

    private Boolean isPublic;
    private Integer version;
    private DocumentStatus status;
    private String phaseId;

    private Double latitude;
    private Double longitude;
    private String locationDescription;  // "2nd floor, kitchen area"

    private String workerId;

    private String inspectorId;
    private String comments;
    private String relatedDocumentId;
    private String comparisonType;

    private DocumentApprovalStatus approvalStatus;

    private String approvedBy;
    private String approverComments;
    private LocalDateTime approvedAt;
    private Integer qualityRating;

    private Integer completionPercentage;
    private String thumbnailPath;
    private String[] keywords;
    private String weatherConditions;
    private Double temperature;

    private String downloadUrl;
}
