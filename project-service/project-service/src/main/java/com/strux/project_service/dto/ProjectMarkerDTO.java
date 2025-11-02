package com.strux.project_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMarkerDTO {

    private String projectId;
    private String projectName;
    private Double latitude;
    private Double longitude;
    private String status;
    private String type;
    private Integer completionPercentage;
    private String companyName;
    private String address;

    // frontend ucun
    public String getMarkerColor() {
        if (completionPercentage == null) return "#808080"; // Gray
        if (completionPercentage == 100) return "#4CAF50"; // Green
        if (completionPercentage >= 50) return "#2196F3"; // Blue
        if (completionPercentage > 0) return "#FF9800"; // Orange
        return "#F44336"; // Red
    }
}
