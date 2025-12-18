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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    // ✅ Demo hesapları whitelist - Sadece bunlar QR ile girebilir
    private static final Set<String> ALLOWED_DEMO_ACCOUNTS = Set.of(
            "demo-admin1@strux.com",
            "demo-admin2@strux.com",
            "demo-admin3@strux.com",
            "demo-worker1@strux.com",
            "demo-worker2@strux.com",
            "demo-worker3@strux.com"
    );

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

    /**
     * ✅ DEMO LOGIN - QR Kod ile direkt giriş
     * Sadece önceden tanımlanmış demo hesapları için çalışır
     */
    @PostMapping("/demo-login")
    public ResponseEntity<LoginResponse> demoLogin(
            @RequestParam String demoToken,
            HttpServletRequest httpRequest) {

        try {
            String ipAddress = getClientIP(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");

            log.info("🎯 Demo login attempt from IP: {}", ipAddress);

            // Demo token'ı decode et
            String email = decodeDemoToken(demoToken);

            // ✅ Güvenlik kontrolü - sadece izin verilen hesaplar
            if (!ALLOWED_DEMO_ACCOUNTS.contains(email)) {
                log.warn("⚠️ Unauthorized demo login attempt: {}", email);
                throw new AuthenticationException("Invalid demo account");
            }

            log.info("✅ Valid demo account: {}", maskEmail(email));

            // Direkt login yap (password check yok)
            LoginResponse response = authService.demoLogin(email, ipAddress, userAgent);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid demo token format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (AuthenticationException e) {
            log.error("❌ Demo login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("❌ Demo login error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Demo token'ı decode et (Base64)
     */
    private String decodeDemoToken(String token) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(token);
            String email = new String(decodedBytes);

            // Email format kontrolü
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                throw new IllegalArgumentException("Invalid email format");
            }

            return email.toLowerCase().trim();

        } catch (IllegalArgumentException e) {
            log.error("Invalid base64 token: {}", token);
            throw new IllegalArgumentException("Invalid demo token format");
        }
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<LoginResponse> verify2FA(
            @Valid @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {

        String twoFAToken = request.get("twoFAToken");
        String code = request.get("code");

        if (twoFAToken == null || code == null) {
            return ResponseEntity.badRequest().build();
        }

        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginResponse response = authService.verify2FA(
                twoFAToken,
                code,
                ipAddress,
                userAgent
        );

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

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(
            @RequestBody Map<String, String> request) {

        String token = request.get("token");
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Verification token is required"));
        }

        authService.verifyEmail(token);
        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {

        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email is required"));
        }

        String ipAddress = getClientIP(httpRequest);

        authService.requestPasswordReset(email, ipAddress);
        return ResponseEntity.ok(Map.of(
                "message", "If this email exists, a password reset link has been sent"
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {

        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Reset token is required"));
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "New password is required"));
        }

        String ipAddress = getClientIP(httpRequest);

        authService.resetPassword(token, newPassword, ipAddress);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(
            @RequestBody Map<String, String> request) {

        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email is required"));
        }

        authService.resendVerificationEmail(email);
        return ResponseEntity.ok(Map.of(
                "message", "If this email exists, a verification link has been sent"
        ));
    }

    @PostMapping("/oauth/code")
    public ResponseEntity<LoginResponse> loginWithAuthCode(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest
    ) {
        String code = request.get("code");
        String redirectUri = request.getOrDefault("redirectUri", "");

        if (code == null || code.isEmpty()) {
            log.error("OAuth code is missing");
            return ResponseEntity.badRequest().build();
        }

        log.info("OAuth callback received - code length: {}", code.length());

        String ipAddress = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        try {
            LoginResponse response = authService.loginWithAuthorizationCode(
                    code,
                    redirectUri,
                    ipAddress,
                    userAgent
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("OAuth login failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponse.builder()
                            .requires2FA(false)
                            .build());
        }
    }

    @GetMapping("/oauth/google/url")
    public ResponseEntity<Map<String, String>> getGoogleLoginUrl(
            @RequestParam(required = false) String redirectUri
    ) {
        String keycloakUrl = System.getenv("KEYCLOAK_SERVER_URL");
        String realm = System.getenv("KEYCLOAK_REALM");
        String clientId = System.getenv("KEYCLOAK_CLIENT_ID");

        if (redirectUri == null || redirectUri.isEmpty()) {
            redirectUri = "http://localhost:5173/login";
        }

        String authUrl = String.format(
                "%s/realms/%s/protocol/openid-connect/auth?client_id=%s&redirect_uri=%s&response_type=code&scope=openid%%20profile%%20email&kc_idp_hint=google",
                keycloakUrl,
                realm,
                clientId,
                redirectUri
        );

        return ResponseEntity.ok(Map.of("url", authUrl));
    }

    // ==================== Helper Methods ====================

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

    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
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

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];

        if (local.length() <= 2) {
            return "**@" + domain;
        }

        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + "@" + domain;
    }
}