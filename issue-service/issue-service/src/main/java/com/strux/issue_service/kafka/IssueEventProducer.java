package com.strux.issue_service.kafka;

import com.strux.issue_service.event.*;
import com.strux.issue_service.model.Issue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class IssueEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishIssueCreatedEvent(Issue issue) {
        IssueCreatedEvent event = new IssueCreatedEvent(
                "issue.created",
                issue.getId(),
                issue.getTitle(),
                issue.getCompanyId(),
                issue.getUserId(),
                issue.getAssignedTo(),
                LocalDateTime.now()
        );
        kafkaTemplate.send("issue.created", event);
        log.info("✅ Issue created event published: {}", issue.getId());
    }

    public void publishIssueUpdatedEvent(Issue issue) {
        IssueUpdatedEvent event = new IssueUpdatedEvent(
                "issue.updated",
                issue.getId(),
                issue.getCompanyId(),
                LocalDateTime.now()
        );
        kafkaTemplate.send("issue.updated", event);
        log.info("✅ Issue updated event published: {}", issue.getId());
    }

    public void publishIssueAssignedEvent(Issue issue, String previousAssignee) {
        IssueAssignedEvent event = new IssueAssignedEvent(
                "issue.assigned",
                issue.getId(),
                issue.getCompanyId(),
                previousAssignee,
                issue.getAssignedTo(),
                LocalDateTime.now()
        );
        kafkaTemplate.send("issue.assigned", event);
        log.info("✅ Issue assigned event published: {}", issue.getId());
    }

    public void publishIssueResolvedEvent(Issue issue) {
        IssueResolvedEvent event = new IssueResolvedEvent(
                "issue.resolved",
                issue.getId(),
                issue.getCompanyId(),
                issue.getResolvedBy(),
                LocalDateTime.now()
        );
        kafkaTemplate.send("issue.resolved", event);
        log.info("✅ Issue resolved event published: {}", issue.getId());
    }

    public void publishIssueClosedEvent(Issue issue) {
        IssueClosedEvent event = new IssueClosedEvent(
                "issue.closed",
                issue.getId(),
                issue.getCompanyId(),
                LocalDateTime.now()
        );
        kafkaTemplate.send("issue.closed", event);
        log.info("✅ Issue closed event published: {}", issue.getId());
    }

    public void publishIssueReopenedEvent(Issue issue) {
        IssueReopenedEvent event = new IssueReopenedEvent(
                "issue.reopened",
                issue.getId(),
                issue.getCompanyId(),
                LocalDateTime.now()
        );
        kafkaTemplate.send("issue.reopened", event);
        log.info("✅ Issue reopened event published: {}", issue.getId());
    }

    public void publishIssueDeletedEvent(Issue issue, boolean hardDelete) {
        IssueDeletedEvent event = new IssueDeletedEvent(
                "issue.deleted",
                issue.getId(),
                issue.getCompanyId(),
                hardDelete,
                LocalDateTime.now()
        );
        kafkaTemplate.send("issue.deleted", event);
        log.info("✅ Issue deleted event published: {}", issue.getId());
    }
}
