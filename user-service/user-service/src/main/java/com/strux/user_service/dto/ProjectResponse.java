package com.strux.user_service.dto;

// WorkerService.java başına import ekle
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProjectResponse {
    private String id;
    private String name;
    private String description;
    private String type;
    private String companyId;
    private LocalDate startDate;
    private LocalDate plannedEndDate;
    private Integer totalUnits;
    private Double latitude;
    private Double longitude;
    private String address;
    private String city;
    private String status;
    private BigDecimal completionPercentage;
}
