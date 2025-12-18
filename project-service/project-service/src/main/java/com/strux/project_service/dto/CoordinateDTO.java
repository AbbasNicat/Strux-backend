package com.strux.project_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CoordinateDTO {
    private Double lat;
    private Double lng;
}
