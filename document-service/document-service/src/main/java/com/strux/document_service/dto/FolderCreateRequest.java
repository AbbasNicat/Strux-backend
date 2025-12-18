package com.strux.document_service.dto;


import com.strux.document_service.enums.EntityType;
import lombok.*;
import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderCreateRequest {

    @NotBlank
    private String name;

    private String description;

    private String parentFolderId;

    @NotBlank
    private String companyId;

    private EntityType entityType;

    private String entityId;
}
