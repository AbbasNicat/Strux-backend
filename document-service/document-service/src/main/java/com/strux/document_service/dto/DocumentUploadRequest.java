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

    // ✅ OPTIONAL - Frontend göndermiyor
    private DocumentType documentType;

    private String folderId;

    private DocumentCategory category;
    private String description;
    private Set<String> tags;

    // ✅ OPTIONAL - Frontend bazen göndermiyor
    private EntityType entityType;

    // ✅ OPTIONAL - Frontend bazen göndermiyor
    private String entityId;

    // ✅ SADECE BU ZORUNLU
    @NotNull(message = "Company ID is required")
    private String companyId;

    private Boolean isPublic;
    private Boolean requiresApproval;  // ✅ Frontend için ekle
}