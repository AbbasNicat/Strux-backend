package com.strux.auth_service.dto;

@lombok.Data
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
public class GoogleUserInfo {

    private String googleId;
    private String email;
    private String name;
    private String picture;
    private boolean emailVerified;
}
