package com.strux.issue_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueStatsResponse {

    private Long totalIssues;
    private Long openIssues;
    private Long inProgressIssues;
    private Long resolvedIssues;
    private Long closedIssues;

    private Map<String, Long> issuesByCategory;
    private Map<String, Long> issuesByType;
    private Map<String, Long> issuesByPriority;
    private Map<String, Long> issuesByStatus;

    private Double averageResolutionTimeHours;
    private Long overdueIssues;
}
