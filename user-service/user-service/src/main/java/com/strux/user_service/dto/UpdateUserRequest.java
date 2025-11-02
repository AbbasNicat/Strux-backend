package com.strux.user_service.dto;


import com.strux.user_service.enums.UserRole;
import com.strux.user_service.model.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(min = 2, max = 50)
    private String firstName;

    @Size(min = 2, max = 50)
    private String lastName;

    private String phone;
    private UserRole role;
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 255, message = "Company ID must not exceed 255 characters")
    private String companyId;

    private String bio;
    private String position;
    private String profileImageUrl;
}
