package com.strux.unit_service.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitReservationRequest {

    @NotBlank(message = "Owner ID is required")
    private String ownerId;

    @NotBlank(message = "Owner name is required")
    @Size(min = 2, max = 100, message = "Owner name must be between 2 and 100 characters")
    private String ownerName;

    @Email(message = "Invalid email format")
    private String ownerEmail;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number format")
    private String ownerPhone;

    private LocalDateTime reservationDate;

    @Min(value = 1, message = "Reservation period must be at least 1 day")
    @Max(value = 365, message = "Reservation period cannot exceed 365 days")
    private Integer reservationDays;

    @DecimalMin(value = "0.0", message = "Deposit amount cannot be negative")
    private BigDecimal depositAmount;

    private String paymentMethod;
    private String paymentReference;

    private String notes;
    private String source;
}