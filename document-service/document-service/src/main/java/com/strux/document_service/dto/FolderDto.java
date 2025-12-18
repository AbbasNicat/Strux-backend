package com.strux.document_service.dto;


import com.strux.document_service.enums.EntityType;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderDto {
    private String id;
    private String name;
    private String description;
    private String parentFolderId;
    private String companyId;
    private EntityType entityType;
    private String entityId;
    private String createdBy;
    private LocalDateTime createdAt;
    private String folderPath;
    private Integer level;
    private Integer documentCount;
    private List<FolderDto> subFolders;
}
