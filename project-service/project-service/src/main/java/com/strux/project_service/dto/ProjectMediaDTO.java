package com.strux.project_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMediaDTO {
    private Long id;
    private String fileName;
    private String fileUrl;
    private String thumbnailUrl;
    private String fileType;
    private String caption;
    private LocalDateTime uploadedAt;
    private String uploadedBy;
}
