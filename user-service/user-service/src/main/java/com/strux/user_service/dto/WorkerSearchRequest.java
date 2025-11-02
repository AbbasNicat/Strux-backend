package com.strux.user_service.dto;

import com.strux.user_service.enums.WorkerSpecialty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerSearchRequest {
    private WorkerSpecialty specialty;
    private String city;
    private Boolean isAvailable;
    private BigDecimal minRating;
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "rating";
    private String sortDirection = "DESC";
}
