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
public class IssueSearchRequest {

    private String keyword;
    private String companyId;
    private String userId;
    private String assignedTo;

    private IssueCategory category;
    private IssueStatus status;
    private IssueType type;

    private String projectId;
    private String taskId;

    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private LocalDateTime dueDateAfter;
    private LocalDateTime dueDateBefore;
}
