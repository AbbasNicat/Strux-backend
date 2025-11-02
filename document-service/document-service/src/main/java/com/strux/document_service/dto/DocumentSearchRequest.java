package com.strux.document_service.dto;

import com.strux.document_service.enums.*;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSearchRequest {

    private String fileName;
    private DocumentType documentType;
    private DocumentCategory category;
    private EntityType entityType;
    private String entityId;
    private String companyId;
    private String uploadedBy;
    private DocumentStatus status;
}
