package com.strux.project_service.event;

import com.strux.project_service.enums.ProjectStatus;
import com.strux.project_service.enums.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectCreatedEvent {

    private String projectId;
    private String companyId;
    private String name;
    private String description;
    private ProjectType type;
    private Integer totalUnits;

    private LocationInfo location;

    private BigDecimal budget;
    private LocalDate startDate;
    private LocalDate plannedEndDate;

    private String createdBy;
    private LocalDateTime timestamp;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LocationInfo {
        private Double latitude;
        private Double longitude;
        private String address;
        private String city;
        private String district;
        private String country;
        private String placeId;
    }
}
