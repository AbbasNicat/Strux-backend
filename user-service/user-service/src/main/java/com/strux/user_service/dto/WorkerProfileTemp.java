package com.strux.user_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkerProfileTemp implements Serializable {
    private static final long serialVersionUID = 1L;

    private String specialty;
    private Integer experienceYears;
    private BigDecimal hourlyRate;
    private String city;
}
