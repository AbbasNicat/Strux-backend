package com.strux.project_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationInfoDTO {

    private String latitude;
    private String longitude;
    private String address;
    private String city;
    private String district;
    private String country;
    private String placeId;
}
