package com.strux.issue_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueResolvedEvent {
    private String eventType = "issue.resolved";
    private String issueId;
    private String companyId;
    private String resolvedBy;
    private LocalDateTime timestamp;
}