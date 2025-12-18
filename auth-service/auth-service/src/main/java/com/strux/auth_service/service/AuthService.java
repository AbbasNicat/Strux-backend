package com.strux.auth_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.strux.auth_service.dto.*;
import com.strux.auth_service.event.UserLoggedInEvent;
import com.strux.auth_service.event.UserRegisteredEvent;
import com.strux.auth_service.exception.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import jakarta.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final KeycloakAdminService keycloakAdminService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AuditLogService auditLogService;
    private final DeviceFingerprintService deviceFingerprintService;
    private final GeoLocationService geoLocationService;
    private final PasswordHistoryService passwordHistoryService;
    private final CaptchaService captchaService;
    private final TwoFactorAuthService twoFactorAuthService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${keycloak.server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${security.max-sessions-per-user:3}")
    private int maxSessionsPerUser;

    @Value("${captcha.enabled:false}")
    private boolean captchaEnabled;

    @Value("${security.blocked-countries:}")
    private List<String> blockedCountries;

    @Value("${security.require-2fa:false}")
    private boolean require2FA;

    @Value("${security.password-history-check:5}")
    private int passwordHistoryCheckCount;

    @Value("${keycloak.use-token-exchange:false}")
    private boolean useTokenExchange;

    // Security Constants
    private static final String LOGIN_ATTEMPTS_PREFIX = "login_attempts:";
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final String SESSION_PREFIX = "session:";
    private static final String CAPTCHA_REQUIRED_PREFIX = "captcha_required:";
    private static final String SUSPICIOUS_IP_PREFIX = "suspicious_ip:";
    private static final String LAST_LOGIN_PREFIX = "last_login:";
    private static final String GOOGLE_KEYS_CACHE_KEY = "google_keys_cache";
    private static final String TEMP_SESSION_PREFIX = "temp_session:";

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCK_TIME_MINUTES = 15;
    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final int MAX_EMAIL_REQUESTS_PER_MINUTE = 15;
    private static final int CAPTCHA_THRESHOLD = 3;
    private static final int MAX_DEVICE_TRUST_DAYS = 30;
    private static final int GOOGLE_KEYS_CACHE_DURATION_HOURS = 24;
    private static final int KAFKA_RETRY_ATTEMPTS = 3;
    private static final int TEMP_SESSION_DURATION_MINUTES = 10;

    private final CompanyInviteService companyInviteService;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{12,}$"
    );

    private volatile Map<String, PublicKey> googlePublicKeysCache;
    private volatile long googleKeysCacheTime;


    // AuthService.java içine eklenecek metod

    @Transactional
    public LoginResponse demoLogin(String email, String ipAddress, String userAgent) {
        try {
            log.info("Demo login attempt - Email: {}", maskEmail(email));

            // Keycloak'dan token al
            Keycloak userKeycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakServerUrl)
                    .realm(realm)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .username(email)
                    .password("Demo123!") // Fixed demo password
                    .build();

            AccessTokenResponse tokenResponse = userKeycloak.tokenManager()
                    .getAccessToken();

            String accessToken = tokenResponse.getToken();
            String refreshToken = tokenResponse.getRefreshToken();

            // User bilgilerini al
            String userId = extractUserIdFromToken(accessToken);

            // Session management
            if (refreshToken != null) {
                manageUserSessions(userId, refreshToken);
            }

            updateLastLoginTime(userId, ipAddress);

            // Audit log
            auditLogService.logSecurityEvent(
                    AuditEvent.LOGIN_SUCCESS,
                    userId,
                    ipAddress,
                    userAgent,
                    Map.of("type", "demo_login", "email", maskEmail(email))
            );

            // Kafka event (optional)
            try {
                UserLoggedInEvent event = new UserLoggedInEvent(
                        userId,
                        email,
                        ipAddress,
                        LocalDateTime.now()
                );
                kafkaTemplate.send("user-logged-in-events", event);
            } catch (Exception e) {
                log.warn("Demo login event publishing failed: {}", e.getMessage());
            }

            TokenPayload payload = extractTokenPayload(accessToken);

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .userId(userId)
                    .email(email)
                    .role(payload.getRole())
                    .requires2FA(false)
                    .build();

        } catch (Exception e) {
            log.error("Demo login failed: {}", e.getMessage());
            throw new AuthenticationException("Demo login failed");
        }
    }

    private String extractUserIdFromToken(String accessToken) {
        try {
            if (accessToken == null || accessToken.isEmpty()) {
                return null;
            }

            String[] tokenParts = accessToken.split("\\.");
            if (tokenParts.length != 3) {
                return null;
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(tokenParts[1]));
            JsonNode payloadNode = objectMapper.readTree(payloadJson);

            return payloadNode.has("sub") ? payloadNode.get("sub").asText() : null;

        } catch (Exception e) {
            log.warn("Could not extract userId from access token: {}", e.getMessage());
            return null;
        }
    }

    @PostConstruct
    public void validateConfiguration() {
        if (require2FA && twoFactorAuthService == null) {
            throw new IllegalStateException("2FA required but TwoFactorAuthService not configured");
        }
        if (keycloakServerUrl == null || keycloakServerUrl.isEmpty()) {
            throw new IllegalStateException("Keycloak server URL must be configured");
        }
        if (clientId == null || clientSecret == null) {
            throw new IllegalStateException("Keycloak client credentials must be configured");
        }
        log.info("AuthService configuration validated successfully");
        log.info("Token Exchange enabled: {}", useTokenExchange);
        log.info("2FA required: {}", require2FA);
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request, String ipAddress, String userAgent, String deviceFingerprint) {
        String userId = null;
        try {
            log.info("Registration initiated - Role: {}, Mode: {}, IP: {}",
                    request.getRole(), request.getMode(), maskIp(ipAddress));

            // ✅ 1. Role-based validation
            request.validateForRole();

            // ✅ 2. Security checks
            if (isBlockedCountry(ipAddress)) {
                auditLogService.logSecurityEvent(AuditEvent.REGISTRATION_BLOCKED_GEO, null, ipAddress, userAgent);
                throw new SecurityException("Registration from your location is not allowed");
            }

            // ✅ 3. Basic validation
            validateRegisterRequest(request);
            validatePasswordStrength(request.getPassword());
            validatePhoneNumber(request.getPhone());

            if (!isValidEmail(request.getEmail())) {
                throw new InvalidInputException("Invalid email format");
            }

            if (passwordHistoryService.isCommonPassword(request.getPassword())) {
                throw new InvalidInputException("This password is too common. Please choose a more unique password");
            }

            // ✅ 4. Company ID determination (COMPANY_ADMIN için)
            String companyId = null;
            String companyName = null;
            String inviteCode = null;

            if (request.getRole() == UserRole.COMPANY_ADMIN) {
                if ("create".equalsIgnoreCase(request.getMode())) {
                    companyId = generateSecureCompanyId();
                    companyName = request.getCompanyName();
                    log.info("Creating new company: {} with ID: {}", companyName, companyId);
                } else if ("join".equalsIgnoreCase(request.getMode())) {
                    companyId = companyInviteService.validateAndUseInviteCode(request.getInviteCode());
                    log.info("User joining existing company: {}", companyId);
                }
            }
            // WORKER veya USER için companyId = null (bağımsız kullanıcılar)

            // ✅ 5. Keycloak'da kullanıcı oluştur
            userId = keycloakAdminService.createUserWithCompanyId(request, companyId);

            // ✅ 6. Role ata
            keycloakAdminService.assignRoleToUser(userId, request.getRole().toString());

            // ✅ 7. Device kaydet
            if (deviceFingerprint != null && !deviceFingerprint.isEmpty()) {
                deviceFingerprintService.registerDevice(userId, deviceFingerprint, ipAddress, userAgent);
            }

            // ✅ 8. Password history kaydet
            passwordHistoryService.addPasswordToHistory(userId, request.getPassword());

            // ✅ 9. İlk admin için invite code oluştur
            if (request.getRole() == UserRole.COMPANY_ADMIN && "create".equalsIgnoreCase(request.getMode())) {
                inviteCode = companyInviteService.generateInviteCode(companyId, userId);
            }

            // ✅ 10. Kafka event publish
            boolean eventPublished = publishUserRegisteredEvent(userId, request, ipAddress, companyId);
            if (!eventPublished) {
                log.error("Failed to publish registration event after retries");
                keycloakAdminService.deleteUser(userId);
                throw new EventPublishException("Event could not be published after retries");
            }

            // ✅ 11. Audit log
            auditLogService.logSecurityEvent(
                    AuditEvent.USER_REGISTERED,
                    userId,
                    ipAddress,
                    userAgent,
                    Map.of(
                            "email", maskEmail(request.getEmail()),
                            "role", request.getRole().toString(),
                            "companyId", companyId != null ? companyId : "independent",
                            "mode", request.getMode() != null ? request.getMode() : "N/A"
                    )
            );

            log.info("Registration completed - UserId: {}, Role: {}, CompanyId: {}",
                    userId, request.getRole(), companyId);

            // ✅ 12. Response oluştur
            String message = buildRegistrationMessage(request.getRole(), inviteCode);

            return new RegisterResponse(
                    userId,
                    request.getEmail(),
                    message,
                    companyId,
                    inviteCode
            );

        } catch (InvalidInputException | EventPublishException | SecurityException e) {
            if (userId != null) {
                cleanupFailedRegistration(userId);
            }
            throw e;
        } catch (Exception e) {
            if (userId != null) {
                cleanupFailedRegistration(userId);
            }
            log.error("Registration error: {}", e.getMessage(), e);
            throw new RegistrationException("Registration failed: " + e.getMessage(), e);
        }
    }

    // ✅ AuthService.java'a EKLENECEK METODLAR

    /**
     * Email verification
     */
    public void verifyEmail(String verificationToken) {
        try {
            keycloakAdminService.verifyEmail(verificationToken);

            auditLogService.logSecurityEvent(
                    AuditEvent.EMAIL_VERIFIED,
                    null,
                    null,
                    null,
                    Map.of("token", verificationToken.substring(0, 8) + "...")
            );

            log.info("Email verified successfully");

        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            log.error("Email verification error: {}", e.getMessage());
            throw new AuthenticationException("Email verification failed", e);
        }
    }

    /**
     * Request password reset - sends email with reset token
     */
    public void requestPasswordReset(String email, String ipAddress) {
        try {
            log.info("Password reset requested for email: {}", maskEmail(email));

            // Rate limiting
            checkAdvancedRateLimit(ipAddress, email);

            // Check if user exists
            List<UserRepresentation> users = keycloakAdminService.searchUsersByEmail(email);

            if (users.isEmpty()) {
                // Don't reveal if email exists or not
                log.warn("Password reset requested for non-existent email: {}", maskEmail(email));

                auditLogService.logSecurityEvent(
                        AuditEvent.PASSWORD_RESET_FAILED,
                        null,
                        ipAddress,
                        null,
                        Map.of("email", maskEmail(email), "reason", "user_not_found")
                );

                // Still return success to prevent email enumeration
                return;
            }

            UserRepresentation user = users.get(0);
            String userId = user.getId();

            // Generate reset token
            String resetToken = UUID.randomUUID().toString();

            // Store reset token in user attributes with expiration
            Map<String, List<String>> attributes = user.getAttributes();
            if (attributes == null) {
                attributes = new HashMap<>();
            }

            attributes.put("password_reset_token", Collections.singletonList(resetToken));
            attributes.put("password_reset_expiry",
                    Collections.singletonList(String.valueOf(System.currentTimeMillis() + (3600000)))); // 1 hour

            user.setAttributes(attributes);
            keycloakAdminService.updateUser(userId, user);

            // Send reset email
            try {
                emailService.sendPasswordResetEmail(email, resetToken);
                log.info("Password reset email sent to: {}", maskEmail(email));
            } catch (Exception e) {
                log.error("Failed to send password reset email: {}", e.getMessage());
                throw new AuthenticationException("Failed to send reset email", e);
            }

            // Audit log
            auditLogService.logSecurityEvent(
                    AuditEvent.PASSWORD_RESET_REQUESTED,
                    userId,
                    ipAddress,
                    null,
                    Map.of("email", maskEmail(email))
            );

        } catch (RateLimitExceededException | AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Password reset request error: {}", e.getMessage());
            throw new AuthenticationException("Password reset request failed", e);
        }
    }

    /**
     * Reset password with token
     */
    public void resetPassword(String resetToken, String newPassword, String ipAddress) {
        try {
            log.info("Password reset attempt with token: {}", resetToken.substring(0, 8) + "...");

            // Validate new password
            validatePasswordStrength(newPassword);

            // Find user by reset token
            RealmResource realmResource = keycloakAdminService.getRealmResource();
            List<UserRepresentation> allUsers = realmResource.users().list();

            UserRepresentation targetUser = null;
            for (UserRepresentation user : allUsers) {
                Map<String, List<String>> attributes = user.getAttributes();
                if (attributes != null && attributes.containsKey("password_reset_token")) {
                    List<String> tokens = attributes.get("password_reset_token");
                    if (tokens != null && tokens.contains(resetToken)) {
                        // Check expiry
                        List<String> expiries = attributes.get("password_reset_expiry");
                        if (expiries != null && !expiries.isEmpty()) {
                            long expiry = Long.parseLong(expiries.get(0));
                            if (System.currentTimeMillis() > expiry) {
                                throw new InvalidTokenException("Reset token has expired");
                            }
                        }
                        targetUser = user;
                        break;
                    }
                }
            }

            if (targetUser == null) {
                auditLogService.logSecurityEvent(
                        AuditEvent.PASSWORD_RESET_FAILED,
                        null,
                        ipAddress,
                        null,
                        Map.of("reason", "invalid_token")
                );
                throw new InvalidTokenException("Invalid or expired reset token");
            }

            String userId = targetUser.getId();


            if (passwordHistoryService.isPasswordInHistory(userId, newPassword, passwordEncoder)) {  // ✅ 3. parametre eklendi
                throw new InvalidInputException("You cannot reuse one of your last " +
                        passwordHistoryCheckCount + " passwords");
            }

            // Check if it's a common password
            if (passwordHistoryService.isCommonPassword(newPassword)) {
                throw new InvalidInputException("This password is too common. Please choose a more unique password");
            }

            // Update password
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(newPassword);
            credential.setTemporary(false);

            keycloakAdminService.getRealmResource()
                    .users()
                    .get(userId)
                    .resetPassword(credential);

            // Add to password history
            passwordHistoryService.addPasswordToHistory(userId, newPassword);

            // Clear reset token
            Map<String, List<String>> attributes = targetUser.getAttributes();
            attributes.remove("password_reset_token");
            attributes.remove("password_reset_expiry");
            targetUser.setAttributes(attributes);
            keycloakAdminService.updateUser(userId, targetUser);

            // Invalidate all sessions (force re-login)
            try {
                keycloakAdminService.getRealmResource()
                        .users()
                        .get(userId)
                        .logout();
            } catch (Exception e) {
                log.warn("Failed to logout user sessions: {}", e.getMessage());
            }

            // Send confirmation email
            try {
                emailService.sendPasswordChangedEmail(targetUser.getEmail());
            } catch (Exception e) {
                log.warn("Failed to send password changed email: {}", e.getMessage());
            }

            // Audit log
            auditLogService.logSecurityEvent(
                    AuditEvent.PASSWORD_RESET_SUCCESS,
                    userId,
                    ipAddress,
                    null,
                    Map.of("email", maskEmail(targetUser.getEmail()))
            );

            log.info("Password reset successful for user: {}", userId);

        } catch (InvalidTokenException | InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            log.error("Password reset error: {}", e.getMessage());
            throw new AuthenticationException("Password reset failed", e);
        }
    }

    /**
     * Resend verification email
     */
    public void resendVerificationEmail(String email) {
        try {
            log.info("Verification email resend requested for: {}", maskEmail(email));

            // Check if user exists
            List<UserRepresentation> users = keycloakAdminService.searchUsersByEmail(email);

            if (users.isEmpty()) {
                // Don't reveal if email exists
                log.warn("Verification resend requested for non-existent email: {}", maskEmail(email));
                return;
            }

            UserRepresentation user = users.get(0);

            // Check if already verified
            if (user.isEmailVerified()) {
                log.info("Email already verified: {}", maskEmail(email));
                return;
            }

            // Generate new token
            String verificationToken = UUID.randomUUID().toString();

            Map<String, List<String>> attributes = user.getAttributes();
            if (attributes == null) {
                attributes = new HashMap<>();
            }

            attributes.put("email_verification_token", Collections.singletonList(verificationToken));
            user.setAttributes(attributes);

            keycloakAdminService.updateUser(user.getId(), user);

            // Send email
            emailService.sendVerificationEmail(email, verificationToken);

            log.info("Verification email resent to: {}", maskEmail(email));

        } catch (Exception e) {
            log.error("Resend verification email error: {}", e.getMessage());
            throw new AuthenticationException("Failed to resend verification email", e);
        }
    }

    private String buildRegistrationMessage(UserRole role, String inviteCode) {
        StringBuilder message = new StringBuilder("Registration completed successfully.");

        switch (role) {
            case COMPANY_ADMIN:
                message.append(" Please verify your email to activate your account.");
                if (inviteCode != null) {
                    message.append(" Your invite code: ").append(inviteCode)
                            .append(" (share this with team members)");
                }
                break;
            case WORKER:
                message.append(" Complete your worker profile to start receiving job opportunities.");
                break;
            case HOMEOWNER:  // ✅ EKLE
                message.append(" You can now browse and hire professionals.");
                break;
            case USER:
                message.append(" You can now browse and hire professionals.");
                break;
            default:
                message.append(" Please verify your email.");
        }

        return message.toString();
    }

    private void cleanupFailedRegistration(String userId) {
        try {
            keycloakAdminService.deleteUser(userId);
        } catch (Exception cleanupEx) {
            log.error("Failed to cleanup user after error: {}", cleanupEx.getMessage());
        }
    }

    private String generateSecureCompanyId() {
        return "company-" + java.util.UUID.randomUUID().toString();
    }

    private boolean publishUserRegisteredEvent(String keycloakId, RegisterRequest request, String ipAddress, String companyId) {
        UserRegisteredEvent.WorkerProfileData workerProfile = null;
        if (request.getRole() == UserRole.WORKER) {
            workerProfile = new UserRegisteredEvent.WorkerProfileData(
                    request.getSpecialty(),
                    request.getExperienceYears(),
                    request.getHourlyRate()
            );
        }
        UserRegisteredEvent event = new UserRegisteredEvent(
                keycloakId,   // ← Keycloak ID
                request.getFirstName(),
                request.getLastName(),
                maskPhone(request.getPhone()),
                request.getEmail(),
                LocalDateTime.now(),
                request.getRole(),
                companyId,
                request.getPosition()
        );

        event.setWorkerProfile(workerProfile);
        event.setCity(request.getCity());
        event.setBio(request.getBio());

        for (int attempt = 1; attempt <= KAFKA_RETRY_ATTEMPTS; attempt++) {
            try {
                kafkaTemplate.send("user-registered-events", event).get(5, TimeUnit.SECONDS);
                log.info("Registration event sent successfully - UserId: {}, Attempt: {}", keycloakId, attempt);
                return true;
            } catch (Exception e) {
                log.warn("Kafka event publishing failed (attempt {}/{}): {}", attempt, KAFKA_RETRY_ATTEMPTS, e.getMessage());
                if (attempt < KAFKA_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(1000L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }
        }
        return false;
    }

    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent,
                               String deviceFingerprint, String captchaToken) throws CaptchaRequiredException {
        try {
            log.info("Login attempt from IP: {}", maskIp(ipAddress));

            // Rate limiting
            checkAdvancedRateLimit(ipAddress, request.getEmail());

            // Geo-blocking
            if (isBlockedCountry(ipAddress)) {
                auditLogService.logSecurityEvent(AuditEvent.LOGIN_BLOCKED_GEO, null, ipAddress, userAgent);
                throw new SecurityException("Login from your location is not allowed");
            }

            // Account lock check
            if (isAccountLocked(request.getEmail())) {
                log.warn("Account locked - Email: {}", maskEmail(request.getEmail()));
                auditLogService.logSecurityEvent(AuditEvent.LOGIN_ACCOUNT_LOCKED, null, ipAddress, userAgent);
                throw new AccountLockedException("Too many failed attempts. Please try again in 15 minutes.");
            }

            // CAPTCHA verification
            if (isCaptchaRequired(request.getEmail()) || isCaptchaRequired(ipAddress)) {
                if (captchaToken == null || !captchaService.verifyCaptcha(captchaToken)) {
                    auditLogService.logSecurityEvent(AuditEvent.LOGIN_CAPTCHA_FAILED, null, ipAddress, userAgent);
                    throw new CaptchaRequiredException("CAPTCHA verification required");
                }
            }

            // Input validation
            if (request.getEmail() == null || request.getPassword() == null) {
                throw new InvalidInputException("Email and password are required");
            }

            // Authenticate with Keycloak
            String tokenUrl = String.format(
                    "%s/realms/%s/protocol/openid-connect/token",
                    keycloakServerUrl, realm
            );

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "password");
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);
            formData.add("username", request.getEmail());
            formData.add("password", request.getPassword());
            formData.add("scope", "openid profile email");

            String response = webClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);

            if (!jsonNode.has("access_token")) {
                recordFailedLogin(request.getEmail(), ipAddress, userAgent);
                throw new AuthenticationException("Invalid credentials");
            }

            String accessToken = jsonNode.get("access_token").asText();
            String refreshToken = jsonNode.has("refresh_token") ? jsonNode.get("refresh_token").asText() : null;
            int expiresIn = jsonNode.get("expires_in").asInt();

            TokenPayload payload = extractTokenPayload(accessToken);

            // Security analysis
            SecurityContext securityContext = analyzeSecurity(payload.getUserId(), ipAddress, userAgent, deviceFingerprint);

            // ✅ 2FA TEMPORARILY DISABLED FOR DEVELOPMENT
            if (false) {  // require2FA || securityContext.isSuspicious()
                String twoFAToken = twoFactorAuthService.generateToken(payload.getUserId());

                // Store temporary session data
                storeTempSessionData(twoFAToken, accessToken, refreshToken, payload);

                auditLogService.logSecurityEvent(
                        AuditEvent.LOGIN_2FA_REQUIRED,
                        payload.getUserId(),
                        ipAddress,
                        userAgent,
                        Map.of("reason", securityContext.getReason())
                );

                return LoginResponse.builder()
                        .requires2FA(true)
                        .twoFAToken(twoFAToken)
                        .userId(payload.getUserId())
                        .email(payload.getEmail())
                        .build();
            }

            // Complete login
            completeLogin(payload, refreshToken, request.getEmail(), deviceFingerprint, ipAddress, userAgent);

            log.info("Login successful - UserId: {}, IP: {}", payload.getUserId(), maskIp(ipAddress));

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(expiresIn)
                    .tokenType("Bearer")
                    .userId(payload.getUserId())
                    .email(payload.getEmail())
                    .role(payload.getRole())
                    .requires2FA(false)
                    .securityWarning(securityContext.getWarningMessage())
                    .build();

        } catch (WebClientResponseException.Unauthorized e) {
            recordFailedLogin(request.getEmail(), ipAddress, userAgent);
            log.warn("Login failed - Unauthorized: Email: {}", maskEmail(request.getEmail()));
            throw new AuthenticationException("Invalid email or password");
        } catch (WebClientResponseException.BadRequest e) {
            String errorBody = e.getResponseBodyAsString();
            log.error("Keycloak 400 Bad Request - Response body: {}", errorBody);
            log.error("Request details - URL: {}/realms/{}/protocol/openid-connect/token",
                    keycloakServerUrl, realm);
            log.error("Client ID: {}, Client Secret exists: {}",
                    clientId, clientSecret != null && !clientSecret.isEmpty());

            recordFailedLogin(request.getEmail(), ipAddress, userAgent);
            throw new AuthenticationException("Login failed: " + errorBody);
        } catch (AccountLockedException | InvalidInputException | AuthenticationException |
                 SecurityException | CaptchaRequiredException e) {
            throw e;
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void completeLogin(TokenPayload payload, String refreshToken, String email,
                               String deviceFingerprint, String ipAddress, String userAgent) {
        // Manage sessions only if refresh token exists
        if (refreshToken != null) {
            manageUserSessions(payload.getUserId(), refreshToken);
        } else {
            log.warn("No refresh token received for user: {}", payload.getUserId());
        }

        clearLoginAttempts(email);
        clearCaptchaRequirement(email);

        if (deviceFingerprint != null && !deviceFingerprint.isEmpty()) {
            deviceFingerprintService.trustDevice(payload.getUserId(), deviceFingerprint);
        }

        updateLastLoginTime(payload.getUserId(), ipAddress);

        // Publish login event (fire and forget)
        try {
            UserLoggedInEvent event = new UserLoggedInEvent(
                    payload.getUserId(),
                    payload.getEmail(),
                    ipAddress,
                    LocalDateTime.now()
            );

            kafkaTemplate.send("user-logged-in-events", event);
        } catch (Exception e) {
            log.error("Login event publishing failed: {}", e.getMessage());
        }

        // Audit log
        auditLogService.logSecurityEvent(
                AuditEvent.LOGIN_SUCCESS,
                payload.getUserId(),
                ipAddress,
                userAgent,
                Map.of(
                        "location", geoLocationService.getLocation(ipAddress),
                        "deviceTrusted", deviceFingerprint != null
                )
        );
    }

    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String userId = null; // ← EKLE
        try {
            log.info("Token refresh initiated");

            if (request.getRefreshToken() == null || request.getRefreshToken().isEmpty()) {
                throw new InvalidInputException("Refresh token is required");
            }

            // ✅ ÖNCELİKLE TOKEN'DAN USER ID ÇEK (expire kontrolü yapmadan)
            try {
                userId = extractUserIdFromRefreshToken(request.getRefreshToken());
            } catch (Exception e) {
                log.warn("Could not extract userId from refresh token: {}", e.getMessage());
                // userId null kalabilir - audit log için
            }

            String tokenUrl = String.format(
                    "%s/realms/%s/protocol/openid-connect/token",
                    keycloakServerUrl, realm
            );

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "refresh_token");
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);
            formData.add("refresh_token", request.getRefreshToken());

            String response = webClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);

            if (!jsonNode.has("access_token")) {
                auditLogService.logSecurityEvent(
                        AuditEvent.TOKEN_REFRESH_FAILED,
                        userId,  // ← ARTIK NULL DEĞİL
                        null,
                        null
                );
                throw new AuthenticationException("Token refresh failed");
            }

            String accessToken = jsonNode.get("access_token").asText();
            String newRefreshToken = jsonNode.get("refresh_token").asText();
            int expiresIn = jsonNode.get("expires_in").asInt();

            TokenPayload payload = extractTokenPayload(accessToken);

            // Update session with new refresh token
            updateSessionToken(payload.getUserId(), request.getRefreshToken(), newRefreshToken);

            auditLogService.logSecurityEvent(
                    AuditEvent.TOKEN_REFRESHED,
                    payload.getUserId(),
                    null,
                    null
            );

            log.info("Token refreshed successfully - UserId: {}", payload.getUserId());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(newRefreshToken)
                    .expiresIn(expiresIn)
                    .tokenType("Bearer")
                    .userId(payload.getUserId())
                    .email(payload.getEmail())
                    .role(payload.getRole())
                    .requires2FA(false)
                    .build();

        } catch (WebClientResponseException.BadRequest e) {
            log.warn("Invalid refresh token - UserId: {}", userId);
            auditLogService.logSecurityEvent(
                    AuditEvent.TOKEN_REFRESH_FAILED,
                    userId,  // ← ÖNCEDEKİ TOKEN'DAN ALINDI
                    null,
                    null
            );
            throw new AuthenticationException("Refresh token is invalid or expired");
        } catch (InvalidInputException | AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token refresh error - UserId: {}: {}", userId, e.getMessage(), e);
            auditLogService.logSecurityEvent(
                    AuditEvent.TOKEN_REFRESH_FAILED,
                    userId,
                    null,
                    null
            );
            throw new AuthenticationException("Token refresh failed: " + e.getMessage(), e);
        }
    }

    // ✅ YENİ HELPER METOD EKLE
    private String extractUserIdFromRefreshToken(String refreshToken) {
        try {
            String[] tokenParts = refreshToken.split("\\.");
            if (tokenParts.length < 2) {
                return null;
            }

            String payload = new String(Base64.getUrlDecoder().decode(tokenParts[1]));
            JsonNode payloadNode = objectMapper.readTree(payload);

            return payloadNode.has("sub") ? payloadNode.get("sub").asText() : null;
        } catch (Exception e) {
            log.debug("Could not parse refresh token: {}", e.getMessage());
            return null;
        }
    }

    @Transactional
    public void logout(String refreshToken, String userId, String ipAddress, String userAgent) {
        try {
            log.info("Logout initiated - UserId: {}", userId);

            if (refreshToken == null || refreshToken.isEmpty()) {
                throw new InvalidInputException("Refresh token is required");
            }

            // Revoke token from Keycloak
            revokeToken(refreshToken);

            // Remove from session cache
            String sessionKey = SESSION_PREFIX + userId;
            removeSessionAtomically(sessionKey, refreshToken);

            auditLogService.logSecurityEvent(
                    AuditEvent.LOGOUT_SUCCESS,
                    userId,
                    ipAddress,
                    userAgent
            );

            log.info("Logout completed successfully - UserId: {}", userId);

        } catch (InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            log.error("Logout error: {}", e.getMessage(), e);
            auditLogService.logSecurityEvent(
                    AuditEvent.LOGOUT_ERROR,
                    userId,
                    ipAddress,
                    userAgent
            );
            throw new AuthenticationException("Logout failed: " + e.getMessage(), e);
        }
    }

    @Transactional
    public LoginResponse googleLogin(String googleToken, String ipAddress, String userAgent,
                                     String deviceFingerprint) {
        try {
            log.info("Google login initiated from IP: {}", maskIp(ipAddress));

            // Security checks
            if (isBlockedCountry(ipAddress)) {
                auditLogService.logSecurityEvent(AuditEvent.LOGIN_BLOCKED_GEO, null, ipAddress, userAgent);
                throw new SecurityException("Login from your location is not allowed");
            }

            checkAdvancedRateLimit(ipAddress, "google_login");

            if (googleToken == null || googleToken.isEmpty()) {
                throw new InvalidInputException("Google token is required");
            }

            // Verify Google token
            GoogleUserInfo googleUserInfo = verifyGoogleToken(googleToken);

            // Find or create user
            String userId = keycloakAdminService.findOrCreateGoogleUser(googleUserInfo);
            UserRepresentation user = keycloakAdminService.getUserById(userId);

            // Security analysis
            SecurityContext securityContext = analyzeSecurity(userId, ipAddress, userAgent, deviceFingerprint);

            // 2FA requirement
            if (securityContext.isSuspicious()) {
                String twoFAToken = twoFactorAuthService.generateToken(userId);

                // Store temporary session for Google login
                storeTempGoogleSession(twoFAToken, userId, user.getEmail());

                auditLogService.logSecurityEvent(
                        AuditEvent.LOGIN_2FA_REQUIRED,
                        userId,
                        ipAddress,
                        userAgent,
                        Map.of("provider", "google", "reason", securityContext.getReason())
                );

                return LoginResponse.builder()
                        .requires2FA(true)
                        .twoFAToken(twoFAToken)
                        .userId(userId)
                        .email(user.getEmail())
                        .build();
            }

            // Get Keycloak token
            LoginResponse loginResponse = getKeycloakTokenForGoogleUser(userId, user.getEmail(), googleToken);

            // Complete login
            if (loginResponse.getRefreshToken() != null) {
                manageUserSessions(userId, loginResponse.getRefreshToken());
            }

            if (deviceFingerprint != null && !deviceFingerprint.isEmpty()) {
                deviceFingerprintService.trustDevice(userId, deviceFingerprint);
            }

            clearLoginAttempts(user.getEmail());
            clearCaptchaRequirement(user.getEmail());
            updateLastLoginTime(userId, ipAddress);

            // Publish event
            try {
                UserLoggedInEvent event = new UserLoggedInEvent(
                        userId,
                        user.getEmail(),
                        ipAddress,
                        LocalDateTime.now()
                );

                kafkaTemplate.send("user-logged-in-events", event);
            } catch (Exception e) {
                log.error("Google login event publishing failed: {}", e.getMessage());
            }

            // Audit log
            auditLogService.logSecurityEvent(
                    AuditEvent.LOGIN_SUCCESS,
                    userId,
                    ipAddress,
                    userAgent,
                    Map.of(
                            "provider", "google",
                            "location", geoLocationService.getLocation(ipAddress),
                            "deviceTrusted", deviceFingerprint != null
                    )
            );

            log.info("Google login successful - UserId: {}, IP: {}", userId, maskIp(ipAddress));

            return loginResponse;

        } catch (InvalidInputException | SecurityException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google login error: {}", e.getMessage(), e);
            auditLogService.logSecurityEvent(AuditEvent.LOGIN_ERROR, null, ipAddress, userAgent);
            throw new AuthenticationException("Google login failed: " + e.getMessage(), e);
        }
    }

    private GoogleUserInfo verifyGoogleToken(String token) {
        try {
            String[] tokenParts = token.split("\\.");
            if (tokenParts.length != 3) {
                throw new AuthenticationException("Invalid token format");
            }

            String headerJson = new String(Base64.getUrlDecoder().decode(tokenParts[0]));
            JsonNode headerNode = objectMapper.readTree(headerJson);
            String kid = headerNode.get("kid").asText();

            String payloadJson = new String(Base64.getUrlDecoder().decode(tokenParts[1]));

            JsonNode payloadNode = objectMapper.readTree(payloadJson);

            // Verify expiration
            long expirationTime = payloadNode.get("exp").asLong();
            if (expirationTime < System.currentTimeMillis() / 1000) {
                throw new AuthenticationException("Google token expired");
            }

            // Verify issuer
            String iss = payloadNode.get("iss").asText();
            if (!iss.contains("accounts.google.com")) {
                throw new AuthenticationException("Invalid token issuer");
            }

            // Verify audience (client ID)
            // Note: You should add your Google Client ID validation here
            // if (payloadNode.has("aud") && !payloadNode.get("aud").asText().equals(googleClientId)) {
            //     throw new AuthenticationException("Invalid token audience");
            // }

            // Get public keys and verify signature
            Map<String, PublicKey> publicKeys = getGooglePublicKeys();
            PublicKey publicKey = publicKeys.get(kid);

            if (publicKey == null) {
                throw new AuthenticationException("Public key not found for token");
            }

            // Verify JWT signature
            try {
                Jwts.parser()
                        .verifyWith((PublicKey) publicKey)
                        .build()
                        .parseSignedClaims(token);
            } catch (JwtException e) {
                throw new AuthenticationException("Invalid token signature", e);
            }

            // Extract user info
            GoogleUserInfo userInfo = new GoogleUserInfo();
            userInfo.setGoogleId(payloadNode.get("sub").asText());
            userInfo.setEmail(payloadNode.get("email").asText());
            userInfo.setName(payloadNode.get("name").asText());
            userInfo.setPicture(payloadNode.has("picture") ? payloadNode.get("picture").asText() : null);
            userInfo.setEmailVerified(payloadNode.get("email_verified").asBoolean());

            log.info("Google token verified successfully - Email: {}", maskEmail(userInfo.getEmail()));
            return userInfo;

        } catch (JwtException | AuthenticationException e) {
            throw new AuthenticationException("Google token verification failed", e);
        } catch (Exception e) {
            log.error("Google token verification error: {}", e.getMessage());
            throw new AuthenticationException("Google token verification failed", e);
        }
    }

    private Map<String, PublicKey> getGooglePublicKeys() {
        long now = System.currentTimeMillis();

        // Check cache first (double-checked locking)
        if (googlePublicKeysCache != null && (now - googleKeysCacheTime) < GOOGLE_KEYS_CACHE_DURATION_HOURS * 3600000L) {
            return googlePublicKeysCache;
        }

        synchronized (this) {
            // Double check after acquiring lock
            if (googlePublicKeysCache != null && (now - googleKeysCacheTime) < GOOGLE_KEYS_CACHE_DURATION_HOURS * 3600000L) {
                return googlePublicKeysCache;
            }

            try {
                String googleKeysUrl = "https://www.googleapis.com/oauth2/v3/certs";

                String keysResponse = webClient.get()
                        .uri(googleKeysUrl)
                        .retrieve()
                        .bodyToMono(String.class)
                        .timeout(Duration.ofSeconds(5))
                        .block();

                JsonNode keysNode = objectMapper.readTree(keysResponse);
                JsonNode keysArray = keysNode.get("keys");

                Map<String, PublicKey> keys = new HashMap<>();

                for (JsonNode keyNode : keysArray) {
                    String kid = keyNode.get("kid").asText();
                    String n = keyNode.get("n").asText();
                    String e = keyNode.get("e").asText();

                    PublicKey publicKey = rsaPublicKeyFromComponents(n, e);
                    keys.put(kid, publicKey);
                }

                googlePublicKeysCache = keys;
                googleKeysCacheTime = now;

                log.info("Google public keys cached successfully");
                return keys;

            } catch (Exception e) {
                log.error("Failed to fetch Google public keys: {}", e.getMessage());
                throw new AuthenticationException("Could not fetch Google public keys", e);
            }
        }
    }

    private PublicKey rsaPublicKeyFromComponents(String modulusB64, String exponentB64) {
        try {
            byte[] decodedModulus = Base64.getUrlDecoder().decode(modulusB64);
            byte[] decodedExponent = Base64.getUrlDecoder().decode(exponentB64);

            java.math.BigInteger modulus = new java.math.BigInteger(1, decodedModulus);
            java.math.BigInteger exponent = new java.math.BigInteger(1, decodedExponent);

            java.security.spec.RSAPublicKeySpec spec = new java.security.spec.RSAPublicKeySpec(modulus, exponent);
            KeyFactory factory = KeyFactory.getInstance("RSA");

            return factory.generatePublic(spec);
        } catch (Exception e) {
            throw new AuthenticationException("Failed to construct RSA public key", e);
        }
    }

    public LoginResponse verify2FA(String twoFAToken, String code, String ipAddress, String userAgent) {
        try {
            // ✅ CRITICAL: Get userId BEFORE verifying (which deletes the token)
            String userId;
            try {
                userId = twoFactorAuthService.getUserIdFromToken(twoFAToken);
            } catch (AuthenticationException e) {
                auditLogService.logSecurityEvent(AuditEvent.LOGIN_2FA_FAILED, null, ipAddress, userAgent);
                throw e;
            }

            // ✅ NOW verify the code (this will delete the token from Redis)
            if (!twoFactorAuthService.verifyToken(twoFAToken, code)) {
                auditLogService.logSecurityEvent(AuditEvent.LOGIN_2FA_FAILED, userId, ipAddress, userAgent);
                throw new AuthenticationException("Invalid 2FA code");
            }

            // Retrieve temp session data
            TempSessionData tempData = getTempSessionData(twoFAToken);

            if (tempData == null) {
                // ✅ User bilgilerini al (Keycloak'dan)
                UserRepresentation user = keycloakAdminService.getUserById(userId);

                // ✅ companyId'yi al
                String companyId = extractCompanyIdFromKeycloakUser(user);

                // ✅ Yeni token oluştur (companyId dahil)
                String accessToken = generateCustomToken(userId, user.getEmail(), companyId);

                // Session management
                updateLastLoginTime(userId, ipAddress);

                auditLogService.logSecurityEvent(
                        AuditEvent.LOGIN_2FA_SUCCESS,
                        userId,
                        ipAddress,
                        userAgent
                );

                return LoginResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(null)
                        .expiresIn(3600)
                        .tokenType("Bearer")
                        .userId(userId)
                        .email(user.getEmail())
                        .role(extractRoleFromKeycloakUser(user))
                        .requires2FA(false)
                        .build();
            }

            // Use stored session data
            if (tempData.getRefreshToken() != null) {
                manageUserSessions(userId, tempData.getRefreshToken());
            }
            updateLastLoginTime(userId, ipAddress);
            clearTempSessionData(twoFAToken);

            auditLogService.logSecurityEvent(
                    AuditEvent.LOGIN_2FA_SUCCESS,
                    userId,
                    ipAddress,
                    userAgent
            );

            return LoginResponse.builder()
                    .accessToken(tempData.getAccessToken())
                    .refreshToken(tempData.getRefreshToken())
                    .expiresIn(tempData.getExpiresIn())
                    .tokenType("Bearer")
                    .userId(tempData.getUserId())
                    .email(tempData.getEmail())
                    .role(tempData.getRole())
                    .requires2FA(false)
                    .build();

        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("2FA verification error: {}", e.getMessage(), e);
            throw new AuthenticationException("2FA verification failed: " + e.getMessage(), e);
        }
    }

    private String extractCompanyIdFromKeycloakUser(UserRepresentation user) {
        if (user.getAttributes() == null) {
            return null;
        }

        List<String> companyIds = user.getAttributes().get("company_id");
        if (companyIds != null && !companyIds.isEmpty()) {
            return companyIds.get(0);
        }

        return null;
    }

    private String extractRoleFromKeycloakUser(UserRepresentation user) {
        if (user.getRealmRoles() != null && !user.getRealmRoles().isEmpty()) {
            String firstRole = user.getRealmRoles().get(0);

            switch (firstRole) {
                case "WORKER": return "WORKER";
                case "MANAGER": return "PROJECT_MANAGER";
                case "OWNER": return "UNIT_OWNER";
                case "ADMIN": return "ADMIN";
                case "COMPANY_ADMIN": return "COMPANY_ADMIN";
                case "VIEWER": return "VIEWER";
                case "HOMEOWNER": return "HOMEOWNER";  // ✅ EKLE
                case "USER": return "USER";
                default: return "USER";
            }
        }

        return "USER";
    }

    private String generateCustomToken(String userId, String email, String companyId) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", userId);
            claims.put("email", email);

            // ✅ companyId ekle
            if (companyId != null && !companyId.isEmpty()) {
                claims.put("companyId", companyId);
            }

            long now = System.currentTimeMillis();
            long expirationTime = now + (3600 * 1000); // 1 hour

            return Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(new Date(now))
                    .setExpiration(new Date(expirationTime))
                    .signWith(SignatureAlgorithm.HS512, clientSecret.getBytes())
                    .compact();

        } catch (Exception e) {
            log.error("Token generation error: {}", e.getMessage());
            throw new AuthenticationException("Could not generate token", e);
        }
    }
    private void updateSessionToken(String userId, String oldRefreshToken, String newRefreshToken) {
        String sessionKey = SESSION_PREFIX + userId;

        String luaScript =
                "local sessions = redis.call('get', KEYS[1]) " +
                        "if sessions then " +
                        "  local success, sessionTable = pcall(cjson.decode, sessions) " +
                        "  if success then " +
                        "    for i, v in ipairs(sessionTable) do " +
                        "      if v == ARGV[1] then " +
                        "        sessionTable[i] = ARGV[2] " +
                        "        redis.call('setex', KEYS[1], 604800, cjson.encode(sessionTable)) " +
                        "        return 1 " +
                        "      end " +
                        "    end " +
                        "  end " +
                        "end " +
                        "return 0";

        try {
            redisTemplate.execute(
                    RedisScript.of(luaScript, Long.class),
                    Collections.singletonList(sessionKey),
                    oldRefreshToken,
                    newRefreshToken
            );
        } catch (Exception e) {
            log.error("Session token update error: {}", e.getMessage());
        }
    }

    private void removeSessionAtomically(String sessionKey, String refreshToken) {
        String luaScript =
                "local sessions = redis.call('get', KEYS[1]) " +
                        "if sessions then " +
                        "  local success, sessionTable = pcall(cjson.decode, sessions) " +
                        "  if success then " +
                        "    for i, v in ipairs(sessionTable) do " +
                        "      if v == ARGV[1] then " +
                        "        table.remove(sessionTable, i) " +
                        "        break " +
                        "      end " +
                        "    end " +
                        "    if #sessionTable == 0 then " +
                        "      redis.call('del', KEYS[1]) " +
                        "    else " +
                        "      redis.call('setex', KEYS[1], 604800, cjson.encode(sessionTable)) " +
                        "    end " +
                        "    return 1 " +
                        "  else " +
                        "    redis.call('del', KEYS[1]) " +
                        "    return 0 " +
                        "  end " +
                        "end " +
                        "return 0";

        try {
            redisTemplate.execute(
                    RedisScript.of(luaScript, Long.class),
                    Collections.singletonList(sessionKey),
                    refreshToken
            );
        } catch (Exception e) {
            log.error("Session removal error: {}", e.getMessage());
        }
    }

    private void revokeToken(String refreshToken) {
        try {
            String logoutUrl = String.format(
                    "%s/realms/%s/protocol/openid-connect/logout",
                    keycloakServerUrl, realm
            );

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);
            formData.add("refresh_token", refreshToken);

            webClient.post()
                    .uri(logoutUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .retry(2)
                    .block();

            log.info("Token revoked successfully");
        } catch (Exception e) {
            log.error("Token revocation error: {}", e.getMessage());
            // Don't throw - logout should succeed even if revocation fails
        }
    }

    private void checkAdvancedRateLimit(String ipAddress, String email) {
        try {
            String ipKey = RATE_LIMIT_PREFIX + "ip:" + ipAddress;
            String emailKey = RATE_LIMIT_PREFIX + "email:" + email;

            // IP rate limit
            Long ipRequests = redisTemplate.opsForValue().increment(ipKey);
            if (ipRequests != null && ipRequests == 1) {
                redisTemplate.expire(ipKey, 60, TimeUnit.SECONDS);
            }

            if (ipRequests != null && ipRequests > MAX_REQUESTS_PER_MINUTE) {
                markSuspiciousIp(ipAddress);
                auditLogService.logSecurityEvent(AuditEvent.RATE_LIMIT_EXCEEDED, null, ipAddress, null);
                throw new RateLimitExceededException("Too many requests from this IP. Please wait.");
            }

            // Email rate limit
            Long emailRequests = redisTemplate.opsForValue().increment(emailKey);
            if (emailRequests != null && emailRequests == 1) {
                redisTemplate.expire(emailKey, 60, TimeUnit.SECONDS);
            }

            if (emailRequests != null && emailRequests > MAX_EMAIL_REQUESTS_PER_MINUTE) {
                auditLogService.logSecurityEvent(AuditEvent.RATE_LIMIT_EXCEEDED, null, ipAddress, null);
                throw new RateLimitExceededException("Too many requests for this account. Please wait.");
            }

            log.debug("Rate limit check passed - IP: {}, Email: {}", maskIp(ipAddress), maskEmail(email));

        } catch (RateLimitExceededException e) {
            throw e;
        } catch (Exception e) {
            log.error("Rate limit check error: {}", e.getMessage(), e);
            // Fail open - don't block user
        }
    }

    private boolean isAccountLocked(String email) {
        try {
            String key = LOGIN_ATTEMPTS_PREFIX + email;
            Object attempts = redisTemplate.opsForValue().get(key);

            if (attempts == null) {
                return false;
            }

            long attemptCount = (attempts instanceof Long)
                    ? (Long) attempts
                    : ((Integer) attempts).longValue();

            return attemptCount >= MAX_LOGIN_ATTEMPTS;
        } catch (Exception e) {
            log.error("Account lock check error: {}", e.getMessage());
            return false; // Fail open
        }
    }

    private void recordFailedLogin(String email, String ipAddress, String userAgent) {
        String key = LOGIN_ATTEMPTS_PREFIX + email;

        String luaScript =
                "local attempts = redis.call('get', KEYS[1]) or '0' " +
                        "attempts = tonumber(attempts) + 1 " +
                        "redis.call('setex', KEYS[1], " + (LOCK_TIME_MINUTES * 60) + ", tostring(attempts)) " +
                        "return attempts";

        try {
            Long attempts = redisTemplate.execute(
                    RedisScript.of(luaScript, Long.class),
                    Collections.singletonList(key)
            );

            if (attempts != null && attempts >= CAPTCHA_THRESHOLD) {
                requireCaptcha(email);
                requireCaptcha(ipAddress);
            }

            auditLogService.logSecurityEvent(
                    AuditEvent.LOGIN_FAILED,
                    null,
                    ipAddress,
                    userAgent,
                    Map.of("email", maskEmail(email), "attempts", attempts)
            );

            log.warn("Failed login attempt - Email: {}, Attempts: {}, IP: {}",
                    maskEmail(email), attempts, maskIp(ipAddress));
        } catch (Exception e) {
            log.error("Failed login record error: {}", e.getMessage());
        }
    }

    private void clearLoginAttempts(String email) {
        try {
            String key = LOGIN_ATTEMPTS_PREFIX + email;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Clear login attempts error: {}", e.getMessage());
        }
    }

    private boolean isCaptchaRequired(String identifier) {
        // ✅ CAPTCHA disabled ise hiç kontrol etme
        if (!captchaEnabled) {  // Bu değişkeni ekle
            return false;
        }

        try {
            String key = CAPTCHA_REQUIRED_PREFIX + identifier;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("CAPTCHA check error: {}", e.getMessage());
            return false;
        }
    }

    private void requireCaptcha(String identifier) {
        try {
            String key = CAPTCHA_REQUIRED_PREFIX + identifier;
            redisTemplate.opsForValue().set(key, true, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("Require CAPTCHA error: {}", e.getMessage());
        }
    }

    private void clearCaptchaRequirement(String email) {
        try {
            String key = CAPTCHA_REQUIRED_PREFIX + email;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Clear CAPTCHA requirement error: {}", e.getMessage());
        }
    }

    private boolean isSuspiciousIp(String ipAddress) {
        try {
            String key = SUSPICIOUS_IP_PREFIX + ipAddress;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Suspicious IP check error: {}", e.getMessage());
            return false;
        }
    }

    private void markSuspiciousIp(String ipAddress) {
        try {
            String key = SUSPICIOUS_IP_PREFIX + ipAddress;
            redisTemplate.opsForValue().set(key, true, 24, TimeUnit.HOURS);
            log.warn("IP marked as suspicious: {}", maskIp(ipAddress));
        } catch (Exception e) {
            log.error("Mark suspicious IP error: {}", e.getMessage());
        }
    }

    private boolean isBlockedCountry(String ipAddress) {
        if (blockedCountries == null || blockedCountries.isEmpty()) {
            return false;
        }
        try {
            String countryCode = geoLocationService.getCountryCode(ipAddress);
            return blockedCountries.contains(countryCode);
        } catch (Exception e) {
            log.error("Country check error: {}", e.getMessage());
            return false; // Fail open
        }
    }

    private LocalDateTime getLastLoginTime(String userId) {
        try {
            String key = LAST_LOGIN_PREFIX + userId;
            Object timestamp = redisTemplate.opsForValue().get(key);

            if (timestamp == null) {
                return null;
            }

            // ✅ Handle different types
            if (timestamp instanceof LocalDateTime) {
                return (LocalDateTime) timestamp;
            }

            // ✅ Handle String (ISO format)
            if (timestamp instanceof String) {
                try {
                    return LocalDateTime.parse((String) timestamp);
                } catch (Exception e) {
                    log.warn("Cannot parse LocalDateTime from string: {}", timestamp);
                    return null;
                }
            }

            // ✅ Handle Long (epoch millis)
            if (timestamp instanceof Long) {
                return LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli((Long) timestamp),
                        java.time.ZoneOffset.UTC
                );
            }

            log.warn("Unknown timestamp type: {}", timestamp.getClass().getName());
            return null;

        } catch (Exception e) {
            log.error("Get last login time error: {}", e.getMessage());
            return null;
        }
    }

    // ✅ updateLastLoginTime metodunu da güncelle
    private void updateLastLoginTime(String userId, String ipAddress) {
        try {
            String key = LAST_LOGIN_PREFIX + userId;
            String locationKey = LAST_LOGIN_PREFIX + userId + ":location";

            // ✅ Store as ISO-8601 string instead of LocalDateTime object
            String isoTimestamp = LocalDateTime.now().toString();

            redisTemplate.opsForValue().set(key, isoTimestamp, 30, TimeUnit.DAYS);
            redisTemplate.opsForValue().set(locationKey, ipAddress, 30, TimeUnit.DAYS);

            log.debug("Last login time updated for user: {}", userId);

        } catch (Exception e) {
            log.error("Update last login time error: {}", e.getMessage());
            // Don't throw - this is not critical
        }
    }

    // ✅ analyzeSecurity metodunu güncelle - geolocation timeout'u handle et
    private SecurityContext analyzeSecurity(String userId, String ipAddress, String userAgent, String deviceFingerprint) {
        SecurityContext context = new SecurityContext();

        // Check device trust
        boolean deviceTrusted = deviceFingerprint != null &&
                !deviceFingerprint.isEmpty() &&
                deviceFingerprintService.isDeviceTrusted(userId, deviceFingerprint);
        context.setDeviceTrusted(deviceTrusted);

        if (!deviceTrusted && deviceFingerprint != null && !deviceFingerprint.isEmpty()) {
            context.setSuspicious(true);
            context.addReason("New device detected");
        }

        // ✅ Check location with error handling
        try {
            String currentLocation = geoLocationService.getCountryCode(ipAddress);
            String lastKnownLocation = geoLocationService.getLastKnownLocation(userId);

            if (lastKnownLocation != null && !currentLocation.equals(lastKnownLocation)) {
                context.setSuspicious(true);
                context.addReason("New location detected: " + currentLocation);
            }

            // Check suspicious IP
            if (isSuspiciousIp(ipAddress)) {
                context.setSuspicious(true);
                context.addReason("Suspicious IP address");
            }

            // Impossible travel detection
            LocalDateTime lastLoginTime = getLastLoginTime(userId);
            if (lastLoginTime != null) {
                long minutesSinceLastLogin = java.time.Duration.between(lastLoginTime, LocalDateTime.now()).toMinutes();

                if (minutesSinceLastLogin < 30 && lastKnownLocation != null && !lastKnownLocation.equals(currentLocation)) {
                    int distance = geoLocationService.calculateDistance(lastKnownLocation, currentLocation);

                    // Check if travel is physically impossible (>500 km/h average speed)
                    double speedKmPerHour = distance / (minutesSinceLastLogin / 60.0);
                    if (speedKmPerHour > 500) {
                        context.setSuspicious(true);
                        context.addReason(String.format("Impossible travel detected: %d km in %d minutes",
                                distance, minutesSinceLastLogin));
                    }
                }
            }

            // Update location for next check
            geoLocationService.updateLocation(userId, currentLocation);

        } catch (Exception e) {
            log.warn("Geolocation check failed, continuing without it: {}", e.getMessage());
            // Continue without geolocation - don't mark as suspicious just because of API failure
        }

        return context;
    }

    // ✅ manageUserSessions metodunu güncelle - better error handling
    private void manageUserSessions(String userId, String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            log.warn("Cannot manage session: refresh token is null for user {}", userId);
            return;
        }

        String sessionKey = SESSION_PREFIX + userId;

        // ✅ Simplified Lua script with better error handling
        String luaScript =
                "local sessions = redis.call('get', KEYS[1]) " +
                        "local sessionTable = {} " +
                        "if sessions then " +
                        "  local success, decoded = pcall(cjson.decode, sessions) " +
                        "  if success and type(decoded) == 'table' then " +
                        "    sessionTable = decoded " +
                        "  else " +
                        "    sessionTable = {} " +
                        "  end " +
                        "end " +
                        "if #sessionTable >= tonumber(ARGV[1]) then " +
                        "  table.remove(sessionTable, 1) " +
                        "end " +
                        "table.insert(sessionTable, ARGV[2]) " +
                        "local encoded = cjson.encode(sessionTable) " +
                        "redis.call('setex', KEYS[1], 604800, encoded) " +
                        "return 1";

        try {
            Long result = redisTemplate.execute(
                    RedisScript.of(luaScript, Long.class),
                    Collections.singletonList(sessionKey),
                    String.valueOf(maxSessionsPerUser),
                    refreshToken
            );

            if (result != null && result == 1L) {
                log.info("Session managed successfully for user: {}", userId);
            } else {
                log.warn("Session management returned unexpected result: {}", result);
            }

        } catch (Exception e) {
            log.error("Session management error for user {}: {} - {}",
                    userId, e.getClass().getSimpleName(), e.getMessage());

            // ✅ Try fallback: simple set without Lua
            try {
                log.info("Attempting fallback session storage for user: {}", userId);
                redisTemplate.opsForValue().set(
                        sessionKey + ":token",
                        refreshToken,
                        7,
                        TimeUnit.DAYS
                );
                log.info("Fallback session storage successful");
            } catch (Exception fallbackEx) {
                log.error("Fallback session storage also failed: {}", fallbackEx.getMessage());
            }
        }
    }

    private void storeTempSessionData(String twoFAToken, String accessToken, String refreshToken, TokenPayload payload) {
        try {
            String key = TEMP_SESSION_PREFIX + twoFAToken;
            TempSessionData data = new TempSessionData(
                    accessToken,
                    refreshToken,
                    3600, // Default expiry
                    payload.getUserId(),
                    payload.getEmail(),
                    payload.getRole()
            );
            redisTemplate.opsForValue().set(key, data, TEMP_SESSION_DURATION_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("Store temp session error: {}", e.getMessage());
        }
    }

    private void storeTempGoogleSession(String twoFAToken, String userId, String email) {
        try {
            String key = TEMP_SESSION_PREFIX + twoFAToken;
            TempSessionData data = new TempSessionData(null, null, 0, userId, email, "BUYER");
            redisTemplate.opsForValue().set(key, data, TEMP_SESSION_DURATION_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("Store temp Google session error: {}", e.getMessage());
        }
    }

    private TempSessionData getTempSessionData(String twoFAToken) {
        try {
            String key = TEMP_SESSION_PREFIX + twoFAToken;
            Object rawData = redisTemplate.opsForValue().get(key);

            if (rawData == null) {
                return null;
            }

            // ✅ If it's already TempSessionData, return it
            if (rawData instanceof TempSessionData) {
                return (TempSessionData) rawData;
            }

            // ✅ If it's a Map (LinkedHashMap), convert it
            if (rawData instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) rawData;

                return new TempSessionData(
                        (String) map.get("accessToken"),
                        (String) map.get("refreshToken"),
                        map.get("expiresIn") != null ? ((Number) map.get("expiresIn")).intValue() : 0,
                        (String) map.get("userId"),
                        (String) map.get("email"),
                        (String) map.get("role")
                );
            }

            // ✅ Try ObjectMapper as fallback
            return objectMapper.convertValue(rawData, TempSessionData.class);

        } catch (Exception e) {
            log.error("Get temp session error: {}", e.getMessage(), e);
            return null;
        }
    }

    private void clearTempSessionData(String twoFAToken) {
        try {
            String key = TEMP_SESSION_PREFIX + twoFAToken;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Clear temp session error: {}", e.getMessage());
        }
    }

    private void validatePhoneNumber(String phone) {
        if (phone != null && !phone.matches("^\\+?[1-9]\\d{1,14}$")) {
            throw new InvalidInputException("Invalid phone number format. Must be E.164 format.");
        }
    }

    private String maskIp(String ip) {
        if (ip == null) return "***";
        String[] parts = ip.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + ".***.***";
        }
        // IPv6 masking
        if (ip.contains(":")) {
            String[] ipv6Parts = ip.split(":");
            if (ipv6Parts.length >= 4) {
                return ipv6Parts[0] + ":" + ipv6Parts[1] + ":***:***";
            }
        }
        return "***";
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return "***" + phone.substring(phone.length() - 4);
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

    private TokenPayload extractTokenPayload(String accessToken) {
        try {
            String[] tokenParts = accessToken.split("\\.");
            if (tokenParts.length != 3) {
                throw new AuthenticationException("Invalid token format");
            }

            String payload = new String(Base64.getUrlDecoder().decode(tokenParts[1]));
            JsonNode payloadNode = objectMapper.readTree(payload);

            // Check token expiration
            if (payloadNode.has("exp")) {
                long expirationTime = payloadNode.get("exp").asLong();
                if (expirationTime < System.currentTimeMillis() / 1000) {
                    throw new AuthenticationException("Token expired");
                }
            }

            String userId = payloadNode.get("sub").asText();
            String email = payloadNode.has("email") ? payloadNode.get("email").asText() : "unknown";

            String role = extractRoleFromRealmAccess(payloadNode);

            return new TokenPayload(userId, email, role);

        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token parse error: {}", e.getMessage());
            throw new AuthenticationException("Token could not be parsed", e);
        }
    }

    private String extractRoleFromRealmAccess(JsonNode payloadNode) {
        try {
            if (payloadNode.has("realm_access") &&
                    payloadNode.get("realm_access").has("roles")) {

                JsonNode rolesNode = payloadNode.get("realm_access").get("roles");

                if (rolesNode.isArray()) {
                    for (JsonNode roleNode : rolesNode) {
                        String keycloakRole = roleNode.asText();

                        switch (keycloakRole) {
                            case "WORKER": return "WORKER";
                            case "MANAGER": return "PROJECT_MANAGER";
                            case "OWNER": return "UNIT_OWNER";
                            case "ADMIN": return "ADMIN";
                            case "COMPANY_ADMIN": return "COMPANY_ADMIN";
                            case "VIEWER": return "VIEWER";
                            case "HOMEOWNER": return "HOMEOWNER";  // ✅ EKLE
                            case "USER": return "USER";
                        }
                    }
                }
            }

            return "USER";

        } catch (Exception e) {
            log.error("Role extraction error: {}", e.getMessage());
            return "USER";
        }
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new InvalidInputException("First name is required");
        }
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            throw new InvalidInputException("Last name is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new InvalidInputException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new InvalidInputException("Password is required");
        }
        if (request.getRole() == null) {
            throw new InvalidInputException("Role is required");
        }
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 12) {
            throw new InvalidInputException("Password must be at least 12 characters long");
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new InvalidInputException(
                    "Password must contain: uppercase letter, lowercase letter, digit, and special character (@#$%^&+=!)"
            );
        }

        // Check for common patterns
        if (password.toLowerCase().contains("password") ||
                password.toLowerCase().contains("qwerty") ||
                password.matches(".*012345.*") ||
                password.matches(".*abcdef.*")) {
            throw new InvalidInputException("Password contains common patterns. Please choose a more secure password.");
        }
    }

    private boolean isValidEmail(String email) {
        if (email == null) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private LoginResponse getKeycloakTokenForGoogleUser(String userId, String email, String googleToken) {
        try {
            if (userId == null || userId.isEmpty() || email == null || email.isEmpty()) {
                throw new InvalidInputException("UserId and email are required");
            }

            String tokenUrl = String.format(
                    "%s/realms/%s/protocol/openid-connect/token",
                    keycloakServerUrl, realm
            );

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

            if (useTokenExchange && googleToken != null) {
                // Use Token Exchange (requires Keycloak configuration)
                formData.add("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange");
                formData.add("client_id", clientId);
                formData.add("client_secret", clientSecret);
                formData.add("subject_token", googleToken);
                formData.add("subject_token_type", "urn:ietf:params:oauth:token-type:access_token");
                formData.add("subject_issuer", "google");
                formData.add("requested_subject", userId);
            } else {
                // Fallback: Use Direct Grant with admin credentials
                // Note: This requires enabling direct grant and having user credentials
                formData.add("grant_type", "password");
                formData.add("client_id", clientId);
                formData.add("client_secret", clientSecret);
                formData.add("username", email);
                // For Google users, we set a random password, so we can't use this method
                // Instead, use admin API to generate token or impersonation

                log.warn("Token exchange not configured. Using fallback method for user: {}", userId);
                return getTokenViaImpersonation(userId, email);
            }

            String response = webClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);

            if (!jsonNode.has("access_token")) {
                throw new AuthenticationException("No access token in response");
            }

            String accessToken = jsonNode.get("access_token").asText();
            String refreshToken = jsonNode.has("refresh_token") && !jsonNode.get("refresh_token").isNull()
                    ? jsonNode.get("refresh_token").asText()
                    : null;
            int expiresIn = jsonNode.has("expires_in") ? jsonNode.get("expires_in").asInt() : 3600;

            TokenPayload payload = extractTokenPayload(accessToken);

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(expiresIn)
                    .tokenType("Bearer")
                    .userId(payload.getUserId())
                    .email(payload.getEmail())
                    .role(payload.getRole())
                    .build();

        } catch (AuthenticationException | InvalidInputException e) {
            throw e;
        } catch (WebClientResponseException e) {
            log.error("Keycloak token error: Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AuthenticationException("Token could not be obtained from Keycloak", e);
        } catch (Exception e) {
            log.error("Keycloak token retrieval error: {}", e.getMessage(), e);
            throw new AuthenticationException("Token could not be obtained", e);
        }
    }

    private LoginResponse getTokenViaImpersonation(String userId, String email) {
        try {
            // This is a fallback that uses Keycloak Admin API to impersonate user
            // Note: Requires proper Keycloak realm configuration for impersonation

            String impersonateUrl = String.format(
                    "%s/admin/realms/%s/users/%s/impersonation",
                    keycloakServerUrl, realm, userId
            );

            // Get admin token first
            String adminToken = keycloakAdminService.getAdminToken();

            String response = webClient.post()
                    .uri(impersonateUrl)
                    .header("Authorization", "Bearer " + adminToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);

            // Parse response and create LoginResponse
            // Note: Response format depends on Keycloak version and configuration

            log.info("Token obtained via impersonation for user: {}", userId);

            // For now, return a basic response
            // In production, you should handle the actual token from Keycloak
            throw new AuthenticationException(
                    "Token exchange not configured. Please enable Token Exchange in Keycloak or configure impersonation properly."
            );

        } catch (Exception e) {
            log.error("Impersonation error: {}", e.getMessage());
            throw new AuthenticationException(
                    "Could not obtain token for Google user. Please configure Token Exchange in Keycloak.", e
            );
        }
    }

    public LoginResponse loginWithAuthorizationCode(
            String code,
            String redirectUri,
            String ipAddress,
            String userAgent
    ) {
        try {
            log.info("Auth code login started from IP: {}", maskIp(ipAddress));

            if (code == null || code.isEmpty()) {
                throw new InvalidInputException("Authorization code is required");
            }

            if (redirectUri == null || redirectUri.isEmpty()) {
                // frontend-dən gəlməsə, server config-dən istifadə et
                redirectUri = "http://localhost:5173/login";
            }

            String tokenUrl = String.format(
                    "%s/realms/%s/protocol/openid-connect/token",
                    keycloakServerUrl, realm
            );

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "authorization_code");
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);
            formData.add("code", code);
            formData.add("redirect_uri", redirectUri);

            String response = webClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);

            if (!jsonNode.has("access_token")) {
                throw new AuthenticationException("No access token in response");
            }

            String accessToken = jsonNode.get("access_token").asText();
            String refreshToken = jsonNode.has("refresh_token") && !jsonNode.get("refresh_token").isNull()
                    ? jsonNode.get("refresh_token").asText()
                    : null;
            int expiresIn = jsonNode.has("expires_in") ? jsonNode.get("expires_in").asInt() : 3600;

            TokenPayload payload = extractTokenPayload(accessToken);

            // session management (mövcud metodu istifadə edirik)
            completeLogin(payload, refreshToken, payload.getEmail(), null, ipAddress, userAgent);

            log.info("Auth code login successful - UserId: {}", payload.getUserId());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(expiresIn)
                    .tokenType("Bearer")
                    .userId(payload.getUserId())
                    .email(payload.getEmail())
                    .role(payload.getRole())
                    .requires2FA(false)
                    .build();

        } catch (InvalidInputException | AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Auth code login error: {}", e.getMessage(), e);
            throw new AuthenticationException("Authorization code login failed: " + e.getMessage(), e);
        }
    }


    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    private static class TokenPayload {
        private String userId;
        private String email;
        private String role;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    private static class TempSessionData implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        private String accessToken;
        private String refreshToken;
        private int expiresIn;
        private String userId;
        private String email;
        private String role;
    }
}