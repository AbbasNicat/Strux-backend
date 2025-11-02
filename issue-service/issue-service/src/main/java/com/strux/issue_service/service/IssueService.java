package com.strux.issue_service.service;

import com.strux.issue_service.dto.*;
import com.strux.issue_service.enums.*;
import com.strux.issue_service.kafka.IssueEventProducer;

import com.strux.issue_service.model.Issue;
import com.strux.issue_service.repository.IssueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IssueService {

    private final IssueRepository issueRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final IssueEventProducer eventPublisher;

    @Transactional
    public IssueDto createIssue(IssueCreateRequest request, String userId) {
        log.info("Creating issue: {} by user: {}", request.getTitle(), userId);

        Issue issue = Issue.builder()
                .userId(userId)
                .companyId(request.getCompanyId())
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .type(request.getType())
                .status(IssueStatus.OPEN)
                .assignedTo(request.getAssignedTo())
                .projectId(request.getProjectId())
                .taskId(request.getTaskId())
                .dueDate(request.getDueDate())
                .build();

        issue = issueRepository.save(issue);

        // Publish event
        eventPublisher.publishIssueCreatedEvent(issue);

        return toDto(issue);
    }

    public IssueDto getIssue(String issueId) {
        Issue issue = issueRepository.findById(issueId)
                .filter(i -> i.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        issueRepository.save(issue);

        return toDto(issue);
    }

    public List<IssueDto> getIssuesByCompany(String companyId) {
        return issueRepository.findByCompanyIdAndDeletedAtIsNull(companyId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<IssueDto> getIssuesByUser(String userId) {
        return issueRepository.findByUserIdAndDeletedAtIsNull(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<IssueDto> getIssuesAssignedToUser(String userId) {
        return issueRepository.findByAssignedToAndDeletedAtIsNull(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<IssueDto> getIssuesByStatus(String companyId, IssueStatus status) {
        return issueRepository.findByCompanyIdAndStatusAndDeletedAtIsNull(companyId, status)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<IssueDto> getIssuesByProject(String projectId) {
        return issueRepository.findByProjectIdAndDeletedAtIsNull(projectId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<IssueDto> getIssuesByTask(String taskId) {
        return issueRepository.findByTaskIdAndDeletedAtIsNull(taskId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<IssueDto> getIssuesByAsset(String assetId) {
        return issueRepository.findByAssetIdAndDeletedAtIsNull(assetId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<IssueDto> getOverdueIssues(String companyId) {
        List<IssueStatus> excludedStatuses = Arrays.asList(
                IssueStatus.RESOLVED,
                IssueStatus.CLOSED,
                IssueStatus.CANCELLED
        );

        return issueRepository.findByCompanyIdAndDueDateBeforeAndStatusNotInAndDeletedAtIsNull(
                        companyId,
                        LocalDateTime.now(),
                        excludedStatuses
                )
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<IssueDto> searchIssues(IssueSearchRequest request) {
        List<Issue> issues = issueRepository.findByCompanyIdAndDeletedAtIsNull(request.getCompanyId());

        return issues.stream()
                .filter(issue -> matchesSearchCriteria(issue, request))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public IssueDto updateIssue(String issueId, IssueUpdateRequest request) {
        Issue issue = issueRepository.findById(issueId)
                .filter(i -> i.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        if (request.getTitle() != null) {
            issue.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            issue.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            issue.setCategory(request.getCategory());
        }
        if (request.getStatus() != null) {
            issue.setStatus(request.getStatus());
        }
        if (request.getType() != null) {
            issue.setType(request.getType());
        }

        if (request.getAssignedTo() != null) {
            issue.setAssignedTo(request.getAssignedTo());
        }
        if (request.getProjectId() != null) {
            issue.setProjectId(request.getProjectId());
        }
        if (request.getTaskId() != null) {
            issue.setTaskId(request.getTaskId());
        }

        issue = issueRepository.save(issue);

        eventPublisher.publishIssueUpdatedEvent(issue);

        return toDto(issue);
    }

    @Transactional
    public IssueDto assignIssue(String issueId, IssueAssignRequest request) {
        Issue issue = issueRepository.findById(issueId)
                .filter(i -> i.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        String previousAssignee = issue.getAssignedTo();
        issue.setAssignedTo(request.getAssignedTo());

        if (issue.getStatus() == IssueStatus.OPEN) {
            issue.setStatus(IssueStatus.IN_PROGRESS);
        }

        issue = issueRepository.save(issue);

        eventPublisher.publishIssueAssignedEvent(issue, previousAssignee);

        return toDto(issue);
    }

    @Transactional
    public IssueDto resolveIssue(String issueId, IssueResolveRequest request, String resolvedBy) {
        Issue issue = issueRepository.findById(issueId)
                .filter(i -> i.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        issue.setStatus(IssueStatus.RESOLVED);
        issue.setResolution(request.getResolution());
        issue.setResolvedBy(resolvedBy);
        issue.setResolvedAt(LocalDateTime.now());

        issue = issueRepository.save(issue);

        eventPublisher.publishIssueResolvedEvent(issue);

        return toDto(issue);
    }

    @Transactional
    public IssueDto closeIssue(String issueId) {
        Issue issue = issueRepository.findById(issueId)
                .filter(i -> i.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        issue.setStatus(IssueStatus.CLOSED);

        issue = issueRepository.save(issue);

        eventPublisher.publishIssueClosedEvent(issue);

        return toDto(issue);
    }

    @Transactional
    public IssueDto reopenIssue(String issueId) {
        Issue issue = issueRepository.findById(issueId)
                .filter(i -> i.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        issue.setStatus(IssueStatus.REOPENED);
        issue.setResolution(null);
        issue.setResolvedBy(null);
        issue.setResolvedAt(null);

        issue = issueRepository.save(issue);

        eventPublisher.publishIssueReopenedEvent(issue);

        return toDto(issue);
    }

    @Transactional
    public void deleteIssue(String issueId, boolean hardDelete) {
        Issue issue = issueRepository.findById(issueId)
                .filter(i -> i.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        if (hardDelete) {
            issueRepository.delete(issue);
        } else {
            issue.setDeletedAt(LocalDateTime.now());
            issueRepository.save(issue);
        }

        eventPublisher.publishIssueDeletedEvent(issue, hardDelete);
    }

    public IssueStatsResponse getIssueStats(String companyId) {
        Long totalIssues = issueRepository.countByCompanyIdAndDeletedAtIsNull(companyId);
        Long openIssues = issueRepository.countByCompanyIdAndStatusAndDeletedAtIsNull(companyId, IssueStatus.OPEN);
        Long inProgressIssues = issueRepository.countByCompanyIdAndStatusAndDeletedAtIsNull(companyId, IssueStatus.IN_PROGRESS);
        Long resolvedIssues = issueRepository.countByCompanyIdAndStatusAndDeletedAtIsNull(companyId, IssueStatus.RESOLVED);
        Long closedIssues = issueRepository.countByCompanyIdAndStatusAndDeletedAtIsNull(companyId, IssueStatus.CLOSED);

        Map<String, Long> issuesByStatus = convertToMap(issueRepository.countByStatusGrouped(companyId));
        Map<String, Long> issuesByCategory = convertToMap(issueRepository.countByCategoryGrouped(companyId));
        Map<String, Long> issuesByType = convertToMap(issueRepository.countByTypeGrouped(companyId));

        Long overdueIssues = (long) getOverdueIssues(companyId).size();

        return IssueStatsResponse.builder()
                .totalIssues(totalIssues)
                .openIssues(openIssues)
                .inProgressIssues(inProgressIssues)
                .resolvedIssues(resolvedIssues)
                .closedIssues(closedIssues)
                .issuesByStatus(issuesByStatus)
                .issuesByCategory(issuesByCategory)
                .issuesByType(issuesByType)
                .overdueIssues(overdueIssues)
                .build();
    }

    // Helper methods

    private boolean matchesSearchCriteria(Issue issue, IssueSearchRequest request) {
        if (request.getKeyword() != null &&
                !issue.getTitle().toLowerCase().contains(request.getKeyword().toLowerCase()) &&
                !issue.getDescription().toLowerCase().contains(request.getKeyword().toLowerCase())) {
            return false;
        }
        if (request.getUserId() != null && !request.getUserId().equals(issue.getUserId())) {
            return false;
        }
        if (request.getAssignedTo() != null && !request.getAssignedTo().equals(issue.getAssignedTo())) {
            return false;
        }
        if (request.getCategory() != null && request.getCategory() != issue.getCategory()) {
            return false;
        }
        if (request.getStatus() != null && request.getStatus() != issue.getStatus()) {
            return false;
        }
        if (request.getType() != null && request.getType() != issue.getType()) {
            return false;
        }

        if (request.getProjectId() != null && !request.getProjectId().equals(issue.getProjectId())) {
            return false;
        }
        if (request.getTaskId() != null && !request.getTaskId().equals(issue.getTaskId())) {
            return false;
        }

        if (request.getCreatedAfter() != null && issue.getCreatedAt().isBefore(request.getCreatedAfter())) {
            return false;
        }
        if (request.getCreatedBefore() != null && issue.getCreatedAt().isAfter(request.getCreatedBefore())) {
            return false;
        }
        return true;
    }

    private Map<String, Long> convertToMap(List<Object[]> results) {
        return results.stream()
                .collect(Collectors.toMap(
                        arr -> arr[0].toString(),
                        arr -> (Long) arr[1]
                ));
    }

    private IssueDto toDto(Issue issue) {
        return IssueDto.builder()
                .id(issue.getId())
                .userId(issue.getUserId())
                .companyId(issue.getCompanyId())
                .assignedTo(issue.getAssignedTo())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .category(issue.getCategory())
                .status(issue.getStatus())
                .type(issue.getType())
                .projectId(issue.getProjectId())
                .taskId(issue.getTaskId())
                .resolution(issue.getResolution())
                .resolvedBy(issue.getResolvedBy())
                .resolvedAt(issue.getResolvedAt())
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .dueDate(issue.getDueDate())
                .build();
    }

}
