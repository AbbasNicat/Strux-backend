package com.strux.issue_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueUpdatedEvent {
    private String eventType = "issue.updated";
    private String issueId;
    private String companyId;
    private LocalDateTime timestamp;
}
