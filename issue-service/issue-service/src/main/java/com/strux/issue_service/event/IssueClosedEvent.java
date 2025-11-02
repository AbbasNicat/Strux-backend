package com.strux.issue_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueClosedEvent {
    private String eventType = "issue.closed";
    private String issueId;
    private String companyId;
    private LocalDateTime timestamp;
}
