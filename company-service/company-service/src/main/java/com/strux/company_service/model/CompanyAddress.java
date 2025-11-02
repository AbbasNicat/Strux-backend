package com.strux.company_service.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class CompanyAddress {

    private String street;

    private String city;

    private String region;

    private String postalCode;

}
