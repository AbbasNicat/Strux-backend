package com.strux.project_service.dto;

import com.strux.project_service.enums.ProjectStatus;
import com.strux.project_service.enums.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMapFilterRequest {

    List<ProjectStatus> statuses;
    List<ProjectType> types;
    List<String> companyIds;
    BigDecimal minCompletion;
    BigDecimal maxCompletion;
}