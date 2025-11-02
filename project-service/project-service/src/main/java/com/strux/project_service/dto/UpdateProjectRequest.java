package com.strux.project_service.dto;

import com.strux.project_service.enums.ProjectStatus;
import com.strux.project_service.enums.ProjectType;
import lombok.Data;


import java.time.LocalDate;

@Data
public class UpdateProjectRequest {

    private String name;

    private String description;

    private ProjectType type;
    private ProjectStatus status;

    private String address;

    private Double latitude;
    private Double longitude;

    private String city;
    private String district;
    private String country;

    private LocalDate startDate;
    private LocalDate plannedEndDate;
    private LocalDate actualEndDate;

    private Integer totalUnits;

    private String imageUrl;
}
