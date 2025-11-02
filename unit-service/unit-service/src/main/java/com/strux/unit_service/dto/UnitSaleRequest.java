package com.strux.unit_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitSaleRequest {

    @NotBlank(message = "Owner ID is required")
    private String ownerId;

    @NotBlank(message = "Owner name is required")
    private String ownerName;

    @Email(message = "Invalid email format")
    private String ownerEmail;

    private String ownerPhone;

    @NotNull(message = "Sale price is required")
    @DecimalMin(value = "0.01")
    private BigDecimal salePrice;

    private LocalDateTime saleDate;
}
