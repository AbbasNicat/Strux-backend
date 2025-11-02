package com.strux.unit_service.dto;

import com.strux.unit_service.enums.ConstructionPhase;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitProgressUpdateRequest {

    @NotNull
    @Min(0) @Max(100)
    private Integer completionPercentage;

    private ConstructionPhase currentPhase;
}
