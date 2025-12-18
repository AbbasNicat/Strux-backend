package com.strux.document_service.model;


import com.strux.document_service.enums.*;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.Indexed;

import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.Set;

@org.springframework.data.mongodb.core.mapping.Document(collection = "documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    private String id;

    @Indexed
    private String fileName;

    private String originalFileName;

    @Indexed
    private String folderId;

    @Indexed
    private String filePath;

    private String bucketName;

    private Long fileSize;

    private String mimeType;

    private String fileExtension;

    @Indexed
    private DocumentType documentType;

    @Indexed
    private DocumentCategory category;

    private String description;

    private Set<String> tags;


    @Indexed
    private EntityType entityType;

    @Indexed
    private String entityId;

    @Indexed
    private String uploadedBy;

    @Indexed
    private String companyId;


    @CreatedDate
    private LocalDateTime uploadedAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private LocalDateTime archivedAt;


    private Boolean isPublic;

    private Integer version;

    @Indexed
    private DocumentStatus status;


    @Indexed
    private String taskId;

    @Indexed
    private String phaseId;

    private Double latitude;
    private Double longitude;
    private String locationDescription;  // "2nd floor, kitchen area"

    @Indexed
    private String workerId;

    private String inspectorId;
    private String comments;
    private String relatedDocumentId;
    private String comparisonType;

    @Indexed
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
}