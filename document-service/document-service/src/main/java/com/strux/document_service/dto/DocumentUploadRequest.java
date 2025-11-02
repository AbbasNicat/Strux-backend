package com.strux.document_service.dto;

import com.strux.document_service.enums.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;


import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadRequest {

    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    private DocumentCategory category;
    private String description;
    private Set<String> tags;

    @NotNull(message = "Entity type is required")
    private EntityType entityType;

    @NotNull(message = "Entity ID is required")
    private String entityId;

    @NotNull(message = "Company ID is required")
    private String companyId;

    private Boolean isPublic;
}
