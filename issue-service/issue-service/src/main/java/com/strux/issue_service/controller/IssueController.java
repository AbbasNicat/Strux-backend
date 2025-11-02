package com.strux.issue_service.controller;

import com.strux.issue_service.dto.*;
import com.strux.issue_service.enums.*;
import com.strux.issue_service.service.IssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
@Slf4j
public class IssueController {

    private final IssueService issueService;

    @PostMapping
    public ResponseEntity<IssueDto> createIssue(
            @RequestBody @Valid IssueCreateRequest request,
            @RequestHeader("X-User-Id") String userId
    ) {
        log.info("Creating issue: {} by user: {}", request.getTitle(), userId);
        IssueDto issue = issueService.createIssue(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(issue);
    }

    @GetMapping("/{issueId}")
    public ResponseEntity<IssueDto> getIssue(@PathVariable String issueId) {
        IssueDto issue = issueService.getIssue(issueId);
        return ResponseEntity.ok(issue);
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<IssueDto>> getIssuesByCompany(@PathVariable String companyId) {
        List<IssueDto> issues = issueService.getIssuesByCompany(companyId);
        return ResponseEntity.ok(issues);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<IssueDto>> getIssuesByUser(@PathVariable String userId) {
        List<IssueDto> issues = issueService.getIssuesByUser(userId);
        return ResponseEntity.ok(issues);
    }

    @GetMapping("/assigned/{userId}")
    public ResponseEntity<List<IssueDto>> getIssuesAssignedToUser(@PathVariable String userId) {
        List<IssueDto> issues = issueService.getIssuesAssignedToUser(userId);
        return ResponseEntity.ok(issues);
    }

    @GetMapping("/company/{companyId}/status/{status}")
    public ResponseEntity<List<IssueDto>> getIssuesByStatus(
            @PathVariable String companyId,
            @PathVariable IssueStatus status
    ) {
        List<IssueDto> issues = issueService.getIssuesByStatus(companyId, status);
        return ResponseEntity.ok(issues);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<IssueDto>> getIssuesByProject(@PathVariable String projectId) {
        List<IssueDto> issues = issueService.getIssuesByProject(projectId);
        return ResponseEntity.ok(issues);
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<IssueDto>> getIssuesByTask(@PathVariable String taskId) {
        List<IssueDto> issues = issueService.getIssuesByTask(taskId);
        return ResponseEntity.ok(issues);
    }

    @GetMapping("/asset/{assetId}")
    public ResponseEntity<List<IssueDto>> getIssuesByAsset(@PathVariable String assetId) {
        List<IssueDto> issues = issueService.getIssuesByAsset(assetId);
        return ResponseEntity.ok(issues);
    }

    @GetMapping("/company/{companyId}/overdue")
    public ResponseEntity<List<IssueDto>> getOverdueIssues(@PathVariable String companyId) {
        List<IssueDto> issues = issueService.getOverdueIssues(companyId);
        return ResponseEntity.ok(issues);
    }

    @PostMapping("/search")
    public ResponseEntity<List<IssueDto>> searchIssues(@RequestBody IssueSearchRequest request) {
        List<IssueDto> issues = issueService.searchIssues(request);
        return ResponseEntity.ok(issues);
    }

    @PutMapping("/{issueId}")
    public ResponseEntity<IssueDto> updateIssue(
            @PathVariable String issueId,
            @RequestBody @Valid IssueUpdateRequest request
    ) {
        IssueDto issue = issueService.updateIssue(issueId, request);
        return ResponseEntity.ok(issue);
    }

    @PutMapping("/{issueId}/assign")
    public ResponseEntity<IssueDto> assignIssue(
            @PathVariable String issueId,
            @RequestBody @Valid IssueAssignRequest request
    ) {
        IssueDto issue = issueService.assignIssue(issueId, request);
        return ResponseEntity.ok(issue);
    }

    @PutMapping("/{issueId}/resolve")
    public ResponseEntity<IssueDto> resolveIssue(
            @PathVariable String issueId,
            @RequestBody @Valid IssueResolveRequest request,
            @RequestHeader("X-User-Id") String userId
    ) {
        IssueDto issue = issueService.resolveIssue(issueId, request, userId);
        return ResponseEntity.ok(issue);
    }

    @PutMapping("/{issueId}/close")
    public ResponseEntity<IssueDto> closeIssue(@PathVariable String issueId) {
        IssueDto issue = issueService.closeIssue(issueId);
        return ResponseEntity.ok(issue);
    }

    @PutMapping("/{issueId}/reopen")
    public ResponseEntity<IssueDto> reopenIssue(@PathVariable String issueId) {
        IssueDto issue = issueService.reopenIssue(issueId);
        return ResponseEntity.ok(issue);
    }

    @DeleteMapping("/{issueId}")
    public ResponseEntity<Void> deleteIssue(
            @PathVariable String issueId,
            @RequestParam(defaultValue = "false") boolean hardDelete
    ) {
        issueService.deleteIssue(issueId, hardDelete);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/company/{companyId}/stats")
    public ResponseEntity<IssueStatsResponse> getIssueStats(@PathVariable String companyId) {
        IssueStatsResponse stats = issueService.getIssueStats(companyId);
        return ResponseEntity.ok(stats);
    }
}