package com.strux.project_service.dto;

import lombok.Data;

@Data
public class UnitDto {
    private String id;
    private String projectId;
    private String unitName;
    private String unitNumber;
    private Integer floor;
    private String blockName;
    private String status;
}
