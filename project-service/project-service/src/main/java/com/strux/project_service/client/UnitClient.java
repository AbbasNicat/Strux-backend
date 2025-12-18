package com.strux.project_service.client;

import com.strux.project_service.config.FeignAuthConfig;
import com.strux.project_service.dto.UnitDto;
import com.strux.project_service.dto.UnitMapInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "unit-service",configuration = FeignAuthConfig.class)
public interface UnitClient {

    @GetMapping("/api/units/project/{projectId}/count")
    Long countUnitsByProject(@PathVariable("projectId") String projectId);

    @GetMapping("/api/units/project/{projectId}/map-info")
    List<UnitMapInfo> getProjectUnitsForMap(@PathVariable("projectId") String projectId);

}



