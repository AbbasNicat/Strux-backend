package com.strux.unit_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildingDetailsDto {
    private UnitDto building;
    private List<UnitDto> floorSchemas;
    private List<UnitDto> subUnits;
}
