package com.strux.project_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLocationRequest {

    private String placeId;
    private String formattedAddress;
    private Double latitude;
    private Double longitude;
    private String city;
    private String region;
    private String district;
    private String country;
    private String postalCode;
}
