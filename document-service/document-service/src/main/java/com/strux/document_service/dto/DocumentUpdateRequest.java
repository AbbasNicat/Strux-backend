package com.strux.document_service.dto;

import com.strux.document_service.enums.*;
import lombok.*;


import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUpdateRequest {

    private DocumentType documentType;
    private DocumentCategory category;
    private String description;
    private Set<String> tags;
    private Boolean isPublic;
}
