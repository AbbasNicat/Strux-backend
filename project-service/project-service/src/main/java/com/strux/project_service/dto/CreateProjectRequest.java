package com.strux.project_service.dto;

import com.strux.project_service.enums.ProjectType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateProjectRequest {

    private String name;

    private String description;

    private ProjectType type;

    private String companyId;

    private String address;

    private Double latitude;

    private Double longitude;

    private String city;
    private String district;
    private String country;
    private String placeId; // Google Maps Place ID

    private LocalDate startDate;

    private LocalDate plannedEndDate;

    private Integer totalUnits;

    private String imageUrl;

    private List<PhaseDefinition> phases;


    @Data
    public static class PhaseDefinition {
        private String phaseName;
        private String description;
        private BigDecimal weightPercentage; // Toplam 100 olmalı
        private LocalDate plannedStartDate;
        private LocalDate plannedEndDate;
        private Integer orderIndex; // phase lerin sirasi
    }
}