package com.strux.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private String userId;
    private String email;
    private String message;
    private String companyId; // ✅ YENİ
    private String inviteCode; // ✅ YENİ - İlk admin için
}