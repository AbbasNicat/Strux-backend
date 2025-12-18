package com.strux.project_service.client;

import com.strux.project_service.config.FeignAuthConfig;
import com.strux.project_service.dto.ProjectWorkerStatsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "user-service",
        configuration = FeignAuthConfig.class
)
public interface WorkerClient {

    @GetMapping("/api/workers/project/{projectId}/stats")
    ProjectWorkerStatsResponse getProjectWorkerStats(@PathVariable("projectId") String projectId);
}



