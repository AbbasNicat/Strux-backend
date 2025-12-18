package com.strux.document_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.strux.document_service.enums.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhancedDocumentUploadRequest {

    @NotNull
    private DocumentType documentType;

    private DocumentCategory category;
    private String description;
    private Set<String> tags;

    private String folderId;

    @NotNull
    private EntityType entityType;

    @NotNull
    private String entityId;

    @NotNull
    private String companyId;

    private Boolean isPublic;


    private String taskId;
    private Integer completionPercentage;
    private String phaseId;

    // Location
    private Double latitude;
    private Double longitude;
    private String locationDescription;

    // Approval workflow
    private Boolean requiresApproval;         // Admin təsdiqi tələb olunur?
    private String[] notifyUsers;             // Kimə notification göndərilsin

    // Metadata
    private String workerId;                  // İşçi ID-si
    private String inspectorId;               // Müfəttiş ID-si (əgər varsa)
    private String comments;                  // İşçi şərhi

    // Related documents (before/after)
    private String relatedDocumentId;         // Əlaqəli sənəd (məs: before photo)
    private String comparisonType;            // "BEFORE", "AFTER", "DURING"
}
