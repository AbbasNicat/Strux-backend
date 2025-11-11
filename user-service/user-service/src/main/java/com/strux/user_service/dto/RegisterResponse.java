package com.strux.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {

    private String userId;

    private String email;

    private String message;

    private String companyId;

    private String inviteCode;

    private boolean success;
}
