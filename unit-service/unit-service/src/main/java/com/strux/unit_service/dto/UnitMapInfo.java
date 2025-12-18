package com.strux.unit_service.dto;

import com.strux.unit_service.enums.UnitType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UnitMapInfo {
    private String id;
    private String unitNumber;
    private String unitName;
    private UnitType type;
    private Double latitude;
    private Double longitude;
    private Integer completionPercentage;
    private Integer subUnitsCount;
    private Boolean hasSubUnits;
}
