package com.strux.issue_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueCommentDto {

    private String id;
    private String issueId;
    private String userId;
    private String comment;
    private LocalDateTime createdAt;
}