package com.strux.project_service.dto;

import com.strux.project_service.model.ProjectPhase;
import com.strux.project_service.model.ScheduleItem;
import jakarta.persistence.*;
import lombok.Builder;

import com.strux.project_service.enums.ProjectStatus;
import com.strux.project_service.enums.ProjectType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ProjectResponse {

    private String id;

    private String companyId;

    private String name;

    private String description;

    private ProjectLocationResponse location;

    private ProjectStatus status;

    private ProjectType type;

    private LocalDate startDate;
    private LocalDate plannedEndDate;
    private LocalDate actualEndDate;

    private Integer totalUnits; // toplam konut

    private BigDecimal overallProgress; // 0-100 arası

    private List<ProjectDetailResponse.PhaseResponse> phases;

    private List<ScheduleItem> schedule;

    private String imageUrl;
}
