package com.strux.issue_service.dto;

import com.strux.issue_service.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueDto {

    private String id;
    private String userId;
    private String companyId;
    private String assignedTo;

    private String title;
    private String description;

    private IssueCategory category;
    private IssueStatus status;
    private IssueType type;

    private String projectId;
    private String taskId;
    private String assetId;

    private String resolution;
    private String resolvedBy;
    private LocalDateTime resolvedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime dueDate;

}
