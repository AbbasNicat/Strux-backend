package com.strux.document_service.dto;

import com.strux.document_service.enums.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnhancedDocumentUploadRequest {

    @NotNull
    private DocumentType documentType;

    private DocumentCategory category;
    private String description;
    private Set<String> tags;

    @NotNull
    private EntityType entityType;

    @NotNull
    private String entityId;

    @NotNull
    private String companyId;

    private Boolean isPublic;

    // === STRUX SPECIFIC FIELDS ===

    // Task/Progress related
    private String taskId;                    // İlişkili task
    private Integer completionPercentage;     // Bu iş ilə tamamlanma faizi
    private String phaseId;                   // İnşaat fazası

    // Location
    private Double latitude;                  // GPS koordinatı
    private Double longitude;                 // GPS koordinatı
    private String locationDescription;       // "2-ci mərtəbə, mətbəx"

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
