package com.strux.issue_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueDeletedEvent {
    private String eventType = "issue.deleted";
    private String issueId;
    private String companyId;
    private boolean hardDelete;
    private LocalDateTime timestamp;
}
