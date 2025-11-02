package com.strux.project_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyInfoDTO {

    private Long id;
    private String name;
    private String taxId;
    private String email;
    private String phone;
    private String website;
    private String logoUrl;
    private Integer activeProjectCount;
    private Double rating;
}
