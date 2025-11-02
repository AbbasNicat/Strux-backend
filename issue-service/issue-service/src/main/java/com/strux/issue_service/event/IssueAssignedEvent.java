package com.strux.issue_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueAssignedEvent {
    private String eventType = "issue.assigned";
    private String issueId;
    private String companyId;
    private String previousAssignee;
    private String newAssignee;
    private LocalDateTime timestamp;
}
