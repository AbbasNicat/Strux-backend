package com.strux.issue_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueCreatedEvent {
    private String eventType = "issue.created";
    private String issueId;
    private String title;
    private String companyId;
    private String userId;
    private String assignedTo;
    private LocalDateTime timestamp;
}