// WorkerClient.java - Task Service

package com.strux.task_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkerClient {

    private final RestTemplate restTemplate;

    @Value("${services.worker.url:http://localhost:9092}")
    private String workerServiceUrl;

    public String getWorkerCurrentUnit(String workerId) {
        try {
            String url = workerServiceUrl + "/api/workers/" + workerId + "/current-unit";
            log.debug("Fetching current unit for worker: {} from {}", workerId, url);

            String unitId = restTemplate.getForObject(url, String.class);

            log.debug("Worker {} is assigned to unit: {}", workerId, unitId);
            return unitId;

        } catch (Exception e) {
            log.warn("Failed to fetch unit for worker {}: {}", workerId, e.getMessage());
            return null;
        }
    }
}