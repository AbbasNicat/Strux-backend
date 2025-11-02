package com.strux.issue_service.model;

import com.strux.issue_service.enums.IssueCategory;
import com.strux.issue_service.enums.IssueStatus;
import com.strux.issue_service.enums.IssueType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Issues")
public class Issue {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String userId; // hansi user
    private String companyId; // hansi company
    private String projectId; // hansi project
    private String taskId; // hansi task uzerinden
    private String assetId;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private IssueCategory category;

    @Enumerated(EnumType.STRING)
    private IssueStatus status;

    @Enumerated(EnumType.STRING)
    private IssueType type;

    private String assignedTo;

    @Column(columnDefinition = "TEXT")
    private String resolution;
    private String resolvedBy;
    private LocalDateTime resolvedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime dueDate;

    private LocalDateTime deletedAt;
}
