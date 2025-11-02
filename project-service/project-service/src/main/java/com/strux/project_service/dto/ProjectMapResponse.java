package com.strux.project_service.dto;

import com.strux.project_service.enums.ProjectStatus;
import com.strux.project_service.enums.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// google map response
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMapResponse {

    private String id;
    private String name;
    private ProjectType type;
    private ProjectStatus status;
    private String latitude;
    private String longitude;
    private String address;
    private String city;

    private ProjectBasicInfoDTO basicInfo;
    private LocationInfoDTO locationInfo;
    // Quick info
    private BigDecimal overallProgress;
    private String imageUrl;

    // Marker icon için
    private String statusColor; // Frontend'de kullanılacak
}
