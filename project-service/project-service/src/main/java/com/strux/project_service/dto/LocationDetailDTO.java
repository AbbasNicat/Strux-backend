package com.strux.project_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDetailDTO {

    private String placeId;
    private String formattedAddress;
    private Double latitude;
    private Double longitude;
    private String district;
    private String city;
    private String region;
    private String country;
    private String postalCode;
}
