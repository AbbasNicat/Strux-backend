package com.strux.project_service.client;

import com.strux.project_service.config.FeignAuthConfig;
import com.strux.project_service.dto.ProjectTaskStatsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "task-service",configuration = FeignAuthConfig.class)
public interface TaskClient {

    @GetMapping("/api/tasks/project/{projectId}/stats")
    ProjectTaskStatsResponse getProjectTaskStats(@PathVariable("projectId") String projectId);
}

