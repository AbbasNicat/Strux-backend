package com.strux.document_service.model;

import com.strux.document_service.enums.EntityType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "folders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Folder {

    @Id
    private String id;

    @Indexed
    private String name;

    private String description;

    @Indexed
    private String parentFolderId;

    @Indexed
    private String companyId;

    @Indexed
    private EntityType entityType;

    @Indexed
    private String entityId;

    private String createdBy;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private Boolean isDeleted;

    private String folderPath;

    private Integer level; // hierarchy seviyesi

}