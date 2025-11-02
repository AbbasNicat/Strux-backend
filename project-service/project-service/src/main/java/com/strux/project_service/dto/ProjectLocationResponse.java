package com.strux.project_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectLocationResponse {
    private Double latitude;
    private Double longitude;
    private String address;
    private String city;
    private String district;
    private String country;
    private String placeId;
}

