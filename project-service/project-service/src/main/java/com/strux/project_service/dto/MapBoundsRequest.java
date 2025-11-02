package com.strux.project_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MapBoundsRequest {
    private Double southWestLat;
    private Double southWestLng;
    private Double northEastLat;
    private Double northEastLng;
}
