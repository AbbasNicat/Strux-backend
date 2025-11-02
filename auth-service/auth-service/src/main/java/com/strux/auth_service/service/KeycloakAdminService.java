package com.strux.auth_service.service;

import com.strux.auth_service.dto.GoogleUserInfo;
import com.strux.auth_service.dto.RegisterRequest;
import com.strux.auth_service.dto.UserRole;
import com.strux.auth_service.exception.UserAlreadyExistsException;
import com.strux.auth_service.exception.KeycloakException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminService {

    private final Keycloak keycloak;
    private final WebClient webClient;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.admin.client-id:admin-cli}")
    private String adminClientId;

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;


    public String createUser(RegisterRequest request) {
        try {
            // Email mövcudluğunu yoxla
            List<UserRepresentation> existingUsers = searchUsersByEmail(request.getEmail());
            if (!existingUsers.isEmpty()) {
                log.warn("User artıq mövcuddur: {}", maskEmail(request.getEmail()));
                throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
            }

            // User representation yarat
            UserRepresentation user = buildUserRepresentation(request);

            // Keycloak-da user yarat
            RealmResource realmResource = getRealmResource();
            UsersResource usersResource = realmResource.users();

            Response response = usersResource.create(user);

            try {
                if (response.getStatus() == 201) {
                    String userId = extractUserIdFromResponse(response);
                    log.info("User uğurla yaradıldı - UserId: {}, Email: {}",
                            userId, maskEmail(request.getEmail()));
                    return userId;
                } else if (response.getStatus() == 409) {
                    throw new UserAlreadyExistsException("User already exists");
                } else {
                    String errorMessage = response.readEntity(String.class);
                    log.error("User yaratma xətası - Status: {}, Error: {}",
                            response.getStatus(), errorMessage);
                    throw new KeycloakException("User could not be created: " + errorMessage);
                }
            } finally {
                response.close();
            }

        } catch (UserAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            log.error("User yaratma xətası: {}", e.getMessage(), e);
            throw new KeycloakException("Failed to create user: " + e.getMessage(), e);
        }
    }

    public String createUser(String email, String password, String firstName, String lastName, String role) {
        try {
            // Email kontrolü
            List<UserRepresentation> existingUsers = searchUsersByEmail(email);
            if (!existingUsers.isEmpty()) {
                log.warn("User already exists: {}", maskEmail(email));
                throw new UserAlreadyExistsException("User with email " + email + " already exists");
            }

            // RegisterRequest oluştur
            RegisterRequest request = RegisterRequest.builder()
                    .email(email)
                    .password(password)
                    .firstName(firstName)
                    .lastName(lastName)
                    .role(UserRole.valueOf(role))
                    .build();

            // Mevcut createUser metodunu kullan
            return createUser(request);

        } catch (UserAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            log.error("User creation error: {}", e.getMessage(), e);
            throw new KeycloakException("Failed to create user: " + e.getMessage(), e);
        }
    }

    /**
     * Google user tap və ya yarat
     */
    public String findOrCreateGoogleUser(GoogleUserInfo googleUserInfo) {
        try {
            // Mövcud user-i tap
            List<UserRepresentation> existingUsers = searchUsersByEmail(googleUserInfo.getEmail());

            if (!existingUsers.isEmpty()) {
                UserRepresentation existingUser = existingUsers.get(0);
                String userId = existingUser.getId();

                // Google identity əlavə et
                updateGoogleAttributes(userId, googleUserInfo);

                log.info("Mövcud user tapıldı və Google identity əlavə edildi: {}", userId);
                return userId;
            }

            // Yeni Google user yarat
            String userId = createGoogleUser(googleUserInfo);
            log.info("Yeni Google user yaradıldı: {}", userId);
            return userId;

        } catch (Exception e) {
            log.error("Google user əməliyyatı xətası: {}", e.getMessage(), e);
            throw new KeycloakException("Google user operation failed: " + e.getMessage(), e);
        }
    }

    /**
     * User-ə rol təyin et
     */
    public void assignRoleToUser(String userId, String roleName) {
        try {
            RealmResource realmResource = getRealmResource();
            UsersResource usersResource = realmResource.users();

            // Role-u tap
            RoleRepresentation role = realmResource.roles()
                    .get(roleName)
                    .toRepresentation();

            // Role-u user-ə təyin et
            usersResource.get(userId)
                    .roles()
                    .realmLevel()
                    .add(Collections.singletonList(role));

            log.info("Rol uğurla təyin edildi - UserId: {}, Role: {}", userId, roleName);

        } catch (Exception e) {
            log.error("Rol təyin etmə xətası - UserId: {}, Role: {}, Error: {}",
                    userId, roleName, e.getMessage());
            throw new KeycloakException("Failed to assign role: " + e.getMessage(), e);
        }
    }

    /**
     * User-i sil
     */
    @CacheEvict(value = "users", key = "#userId")
    public void deleteUser(String userId) {
        try {
            RealmResource realmResource = getRealmResource();
            UsersResource usersResource = realmResource.users();

            Response response = usersResource.delete(userId);

            try {
                if (response.getStatus() == 204) {
                    log.info("User uğurla silindi: {}", userId);
                } else {
                    log.warn("User silinmə statusu: {} - UserId: {}", response.getStatus(), userId);
                }
            } finally {
                response.close();
            }

        } catch (Exception e) {
            log.error("User silmə xətası - UserId: {}, Error: {}", userId, e.getMessage());
            throw new KeycloakException("Failed to delete user: " + e.getMessage(), e);
        }
    }

    /**
     * User-i ID ilə tap
     */
    @Cacheable(value = "users", key = "#userId")
    public UserRepresentation getUserById(String userId) {
        try {
            return getRealmResource().users().get(userId).toRepresentation();
        } catch (Exception e) {
            log.error("User tapılmadı - UserId: {}, Error: {}", userId, e.getMessage());
            throw new KeycloakException("User not found: " + userId, e);
        }
    }

    /**
     * Email ilə user axtar
     */
    public List<UserRepresentation> searchUsersByEmail(String email) {
        try {
            return getRealmResource().users().search(email, true);
        } catch (Exception e) {
            log.error("User axtarış xətası - Email: {}, Error: {}", maskEmail(email), e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * User məlumatlarını yenilə
     */
    @CacheEvict(value = "users", key = "#userId")
    public void updateUser(String userId, UserRepresentation user) {
        try {
            RealmResource realmResource = getRealmResource();
            realmResource.users().get(userId).update(user);
            log.info("User məlumatları yeniləndi: {}", userId);

        } catch (Exception e) {
            log.error("User yeniləmə xətası - UserId: {}, Error: {}", userId, e.getMessage());
            throw new KeycloakException("Failed to update user: " + e.getMessage(), e);
        }
    }

    /**
     * User password dəyişdir
     */
    public void resetPassword(String userId, String newPassword, boolean temporary) {
        try {
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(newPassword);
            credential.setTemporary(temporary);

            getRealmResource().users().get(userId).resetPassword(credential);

            log.info("Password uğurla dəyişdirildi - UserId: {}, Temporary: {}", userId, temporary);

        } catch (Exception e) {
            log.error("Password dəyişdirmə xətası - UserId: {}, Error: {}", userId, e.getMessage());
            throw new KeycloakException("Failed to reset password: " + e.getMessage(), e);
        }
    }

    /**
     * Email verification göndər
     */
    public void sendVerificationEmail(String userId) {
        try {
            getRealmResource().users().get(userId).sendVerifyEmail();
            log.info("Verification email göndərildi - UserId: {}", userId);

        } catch (Exception e) {
            log.error("Verification email göndərmə xətası - UserId: {}, Error: {}",
                    userId, e.getMessage());
            throw new KeycloakException("Failed to send verification email: " + e.getMessage(), e);
        }
    }

    /**
     * User-i enable/disable et
     */
    @CacheEvict(value = "users", key = "#userId")
    public void setUserEnabled(String userId, boolean enabled) {
        try {
            UserRepresentation user = getUserById(userId);
            user.setEnabled(enabled);
            updateUser(userId, user);

            log.info("User status dəyişdirildi - UserId: {}, Enabled: {}", userId, enabled);

        } catch (Exception e) {
            log.error("User status dəyişdirmə xətası - UserId: {}, Error: {}",
                    userId, e.getMessage());
            throw new KeycloakException("Failed to set user status: " + e.getMessage(), e);
        }
    }

    /**
     * Admin token əldə et
     */
    public String getAdminToken() {
        try {
            String tokenUrl = String.format(
                    "%s/realms/master/protocol/openid-connect/token",
                    keycloakServerUrl
            );

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "password");
            formData.add("client_id", adminClientId);
            formData.add("username", adminUsername);
            formData.add("password", adminPassword);

            String response = webClient.post()
                    .uri(tokenUrl)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            // Parse token from response
            // You'll need to add JSON parsing here
            return response; // Simplified

        } catch (Exception e) {
            log.error("Admin token əldə etmə xətası: {}", e.getMessage());
            throw new KeycloakException("Failed to get admin token", e);
        }
    }

    /**
     * User-in rollarını əldə et
     */
    public List<String> getUserRoles(String userId) {
        try {
            List<RoleRepresentation> roles = getRealmResource()
                    .users()
                    .get(userId)
                    .roles()
                    .realmLevel()
                    .listEffective();

            return roles.stream()
                    .map(RoleRepresentation::getName)
                    .toList();

        } catch (Exception e) {
            log.error("User rolları əldə etmə xətası - UserId: {}", userId);
            return Collections.emptyList();
        }
    }

    // ==================== Private Helper Methods ====================

    private UserRepresentation buildUserRepresentation(RegisterRequest request) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(request.getEmail());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmailVerified(false);

        // Credential yarat
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.getPassword());
        credential.setTemporary(false);
        user.setCredentials(Collections.singletonList(credential));

        // Attributes əlavə et
        Map<String, List<String>> attributes = new HashMap<>();
        if (request.getPhone() != null) {
            attributes.put("phone", Collections.singletonList(request.getPhone()));
        }
        if (request.getCompanyId() != null) {
            attributes.put("companyId", Collections.singletonList(request.getCompanyId()));
        }
        if (request.getPosition() != null) {
            attributes.put("position", Collections.singletonList(request.getPosition()));
        }
        user.setAttributes(attributes);

        return user;
    }

    private String createGoogleUser(GoogleUserInfo googleUserInfo) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(googleUserInfo.getEmail());
        user.setEmail(googleUserInfo.getEmail());
        user.setEmailVerified(googleUserInfo.isEmailVerified());

        // Parse name
        String[] nameParts = googleUserInfo.getName().split(" ", 2);
        user.setFirstName(nameParts[0]);
        user.setLastName(nameParts.length > 1 ? nameParts[1] : "");

        // Random password (user won't use it)
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(UUID.randomUUID().toString());
        credential.setTemporary(false);
        user.setCredentials(Collections.singletonList(credential));

        // Google attributes
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("google_id", Collections.singletonList(googleUserInfo.getGoogleId()));
        attributes.put("auth_provider", Collections.singletonList("google"));
        if (googleUserInfo.getPicture() != null) {
            attributes.put("picture_url", Collections.singletonList(googleUserInfo.getPicture()));
        }
        user.setAttributes(attributes);

        RealmResource realmResource = getRealmResource();
        UsersResource usersResource = realmResource.users();

        Response response = usersResource.create(user);

        try {
            if (response.getStatus() == 201) {
                String userId = extractUserIdFromResponse(response);
                assignRoleToUser(userId, "BUYER"); // Default role
                return userId;
            } else {
                String errorMessage = response.readEntity(String.class);
                throw new KeycloakException("Google user could not be created: " + errorMessage);
            }
        } finally {
            response.close();
        }
    }

    private void updateGoogleAttributes(String userId, GoogleUserInfo googleUserInfo) {
        try {
            UserRepresentation user = getUserById(userId);

            Map<String, List<String>> attributes = user.getAttributes();
            if (attributes == null) {
                attributes = new HashMap<>();
            }

            attributes.put("google_id", Collections.singletonList(googleUserInfo.getGoogleId()));
            attributes.put("auth_provider", Collections.singletonList("google"));
            if (googleUserInfo.getPicture() != null) {
                attributes.put("picture_url", Collections.singletonList(googleUserInfo.getPicture()));
            }

            user.setAttributes(attributes);
            updateUser(userId, user);

        } catch (Exception e) {
            log.warn("Google attributes əlavə edilə bilmədi - UserId: {}, Error: {}",
                    userId, e.getMessage());
            // Don't throw - this is not critical
        }
    }

    private RealmResource getRealmResource() {
        try {
            return keycloak.realm(realm);
        } catch (Exception e) {
            log.error("Realm resource əldə etmə xətası: {}", e.getMessage());
            throw new KeycloakException("Failed to get realm resource", e);
        }
    }

    private String extractUserIdFromResponse(Response response) {
        String location = response.getLocation().getPath();
        return location.replaceAll(".*/users/", "");
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