package com.strux.company_service.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.math.BigDecimal;

@Embeddable
@Data
public class CompanyFinancials {

    private String bankName;

    private String bankAccountNumber;

    private String iban;

    private String swift;

    private BigDecimal annualRevenue;

    private String currency;
}