package com.strux.unit_service.dto;

import com.strux.unit_service.enums.UnitType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UnitMapGeometry {
    private String id;
    private String parentUnitId;
    private String unitName;
    private UnitType type;

    private Double latitude;
    private Double longitude;

    private String footprintJson; // polygon və ya shape

    private Integer completionPercentage;
}

