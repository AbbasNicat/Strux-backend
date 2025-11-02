package com.strux.auth_service.controller;

import com.strux.auth_service.dto.*;
import com.strux.auth_service.exception.AuthenticationException;
import com.strux.auth_service.exception.CaptchaRequiredException;
import com.strux.auth_service.service.AuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        String deviceFingerprint = httpRequest.getHeader("X-Device-Fingerprint");

        RegisterResponse response = authService.register(request, ipAddress, userAgent, deviceFingerprint);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) throws CaptchaRequiredException {

        String ipAddress = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        String deviceFingerprint = httpRequest.getHeader("X-Device-Fingerprint");
        String captchaToken = httpRequest.getHeader("X-Captcha-Token");

        LoginResponse response = authService.login(request, ipAddress, userAgent, deviceFingerprint, captchaToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<LoginResponse> verify2FA(
            @Valid @RequestBody TwoFactorVerifyRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginResponse response = authService.verify2FA(request.getTwoFAToken(), request.getCode(), ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        String authHeader = httpRequest.getHeader("Authorization");
        String accessToken = authHeader != null && authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : null;

        String userId = extractUserIdFromToken(accessToken);

        authService.logout(request.getRefreshToken(), userId, ipAddress, userAgent);
        return ResponseEntity.noContent().build();
    }

    private String extractUserIdFromToken(String token) {
        try {
            String[] tokenParts = token.split("\\.");
            String payload = new String(java.util.Base64.getUrlDecoder().decode(tokenParts[1]));
            JsonNode payloadNode = new ObjectMapper().readTree(payload);
            return payloadNode.get("sub").asText();
        } catch (Exception e) {
            throw new AuthenticationException("Could not extract userId from token");
        }
    }

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        String deviceFingerprint = httpRequest.getHeader("X-Device-Fingerprint");

        LoginResponse response = authService.googleLogin(
                request.getGoogleToken(),
                ipAddress,
                userAgent,
                deviceFingerprint
        );
        return ResponseEntity.ok(response);
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            String[] ips = xForwardedFor.split(",");
            return ips[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp.trim();
        }

        return request.getRemoteAddr();
    }
}
