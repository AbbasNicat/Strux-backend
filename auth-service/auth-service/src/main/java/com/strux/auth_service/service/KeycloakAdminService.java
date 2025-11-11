package com.strux.auth_service.service;

import com.strux.auth_service.dto.GoogleUserInfo;
import com.strux.auth_service.dto.RegisterRequest;
import com.strux.auth_service.dto.UserRole;
import com.strux.auth_service.exception.InvalidTokenException;
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

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminService {

    private final Keycloak keycloak;
    private final EmailService emailService;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Company ID ilə user yarat (Email verification optional)
     */
    public String createUserWithCompanyId(RegisterRequest request, String companyId) {
        try {
            List<UserRepresentation> existingUsers = searchUsersByEmail(request.getEmail());
            if (!existingUsers.isEmpty()) {
                log.warn("User already exists: {}", maskEmail(request.getEmail()));
                throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
            }

            UserRepresentation user = new UserRepresentation();
            user.setEnabled(true);
            user.setUsername(request.getEmail());
            user.setEmail(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmailVerified(false); // Email verify lazımdır

            // Credential
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(request.getPassword());
            credential.setTemporary(false);
            user.setCredentials(Collections.singletonList(credential));

            // Attributes
            Map<String, List<String>> attributes = new HashMap<>();

            if (companyId != null) {
                attributes.put("company_id", Collections.singletonList(companyId));
            }
            if (request.getPhone() != null) {
                attributes.put("phone", Collections.singletonList(request.getPhone()));
            }
            if (request.getPosition() != null) {
                attributes.put("position", Collections.singletonList(request.getPosition()));
            }
            if (request.getCompanyName() != null) {
                attributes.put("company_name", Collections.singletonList(request.getCompanyName()));
            }

            // ✅ Email verification token əlavə et
            String verificationToken = UUID.randomUUID().toString();
            attributes.put("email_verification_token", Collections.singletonList(verificationToken));
            attributes.put("email_verification_status", Collections.singletonList("PENDING"));

            user.setAttributes(attributes);

            // ✅ Required actions-ı clear et (login üçün məcburi olmasın)
            user.setRequiredActions(Collections.emptyList());

            // Keycloak'da yarat
            RealmResource realmResource = getRealmResource();
            UsersResource usersResource = realmResource.users();

            Response response = usersResource.create(user);

            try {
                if (response.getStatus() == 201) {
                    String userId = extractUserIdFromResponse(response);

                    // ✅ Custom verification email göndər
                    try {
                        emailService.sendVerificationEmail(request.getEmail(), verificationToken);
                        log.info("Verification email sent to: {}", maskEmail(request.getEmail()));
                    } catch (Exception e) {
                        log.warn("Failed to send verification email: {}", e.getMessage());
                        // Don't throw - user can still login
                    }

                    log.info("User created - UserId: {}, Email: {}, CompanyId: {}",
                            userId, maskEmail(request.getEmail()), companyId);
                    return userId;
                } else if (response.getStatus() == 409) {
                    throw new UserAlreadyExistsException("User already exists");
                } else {
                    String errorMessage = response.readEntity(String.class);
                    log.error("User creation error - Status: {}, Error: {}",
                            response.getStatus(), errorMessage);
                    throw new KeycloakException("User could not be created: " + errorMessage);
                }
            } finally {
                response.close();
            }

        } catch (UserAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            log.error("User creation error: {}", e.getMessage(), e);
            throw new KeycloakException("Failed to create user: " + e.getMessage(), e);
        }
    }

    public String createUser(String email, String password, String firstName, String lastName, UserRole role) {
        try {
            // 1) Var mı kontrol
            var existing = searchUsersByEmail(email);
            if (!existing.isEmpty()) {
                throw new com.strux.auth_service.exception.UserAlreadyExistsException("User already exists: " + email);
            }

            // 2) UserRepresentation hazırla
            UserRepresentation user = new UserRepresentation();
            user.setEnabled(true);
            user.setUsername(email);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmailVerified(false); // verify mail akışın varsa false kalsın

            CredentialRepresentation cred = new CredentialRepresentation();
            cred.setType(CredentialRepresentation.PASSWORD);
            cred.setValue(password);
            cred.setTemporary(false);
            user.setCredentials(java.util.Collections.singletonList(cred));

            // 3) Keycloak’a yarat
            var realmResource = getRealmResource();
            var users = realmResource.users();
            var response = users.create(user);

            try {
                int status = response.getStatus();
                if (status == 201) {
                    String userId = extractUserIdFromResponse(response);

                    // 4) Rol ata (boşsa USER)
                    String roleName = (role != null) ? role.name() : "USER";
                    assignRoleToUser(userId, roleName);

                    // (opsiyonel) verify e-mail gönder
                    // sendVerificationEmail(userId);

                    log.info("User created in Keycloak. userId={}, email={}", userId, maskEmail(email));
                    return userId;
                } else if (status == 409) {
                    throw new com.strux.auth_service.exception.UserAlreadyExistsException("User already exists: " + email);
                } else {
                    String err = response.readEntity(String.class);
                    throw new com.strux.auth_service.exception.KeycloakException("Create user failed: " + err);
                }
            } finally {
                response.close();
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("createUser error: {}", e.getMessage(), e);
            throw new com.strux.auth_service.exception.KeycloakException("Create user failed", e);
        }
    }

    /**
     * Email verification
     */
    public void verifyEmail(String verificationToken) {
        try {
            RealmResource realmResource = getRealmResource();

            // Token ilə user tap
            List<UserRepresentation> allUsers = realmResource.users().list();
            UserRepresentation targetUser = null;

            for (UserRepresentation user : allUsers) {
                Map<String, List<String>> attributes = user.getAttributes();
                if (attributes != null && attributes.containsKey("email_verification_token")) {
                    List<String> tokens = attributes.get("email_verification_token");
                    if (tokens != null && tokens.contains(verificationToken)) {
                        targetUser = user;
                        break;
                    }
                }
            }

            if (targetUser == null) {
                throw new InvalidTokenException("Invalid verification token");
            }

            // Status-u yenilə
            Map<String, List<String>> attributes = targetUser.getAttributes();
            attributes.put("email_verification_status", Collections.singletonList("VERIFIED"));
            attributes.remove("email_verification_token");

            // Keycloak-da email verified et
            targetUser.setEmailVerified(true);
            targetUser.setAttributes(attributes);

            updateUser(targetUser.getId(), targetUser);

            log.info("Email verified for user: {}", targetUser.getId());

        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            log.error("Email verification error: {}", e.getMessage());
            throw new KeycloakException("Failed to verify email", e);
        }
    }

    /**
     * User-ə rol təyin et
     */
    public void assignRoleToUser(String userId, String roleName) {
        try {
            RealmResource realmResource = getRealmResource();
            UsersResource usersResource = realmResource.users();

            RoleRepresentation role = realmResource.roles()
                    .get(roleName)
                    .toRepresentation();

            usersResource.get(userId)
                    .roles()
                    .realmLevel()
                    .add(Collections.singletonList(role));

            log.info("Role assigned - UserId: {}, Role: {}", userId, roleName);

        } catch (Exception e) {
            log.error("Role assignment error - UserId: {}, Role: {}, Error: {}",
                    userId, roleName, e.getMessage());
            throw new KeycloakException("Failed to assign role: " + e.getMessage(), e);
        }
    }

    @CacheEvict(value = "users", key = "#userId")
    public void deleteUser(String userId) {
        try {
            RealmResource realmResource = getRealmResource();
            UsersResource usersResource = realmResource.users();

            Response response = usersResource.delete(userId);

            try {
                if (response.getStatus() == 204) {
                    log.info("User deleted: {}", userId);
                } else {
                    log.warn("User deletion status: {} - UserId: {}", response.getStatus(), userId);
                }
            } finally {
                response.close();
            }

        } catch (Exception e) {
            log.error("User deletion error - UserId: {}, Error: {}", userId, e.getMessage());
            throw new KeycloakException("Failed to delete user: " + e.getMessage(), e);
        }
    }

    @Cacheable(value = "users", key = "#userId")
    public UserRepresentation getUserById(String userId) {
        try {
            return getRealmResource().users().get(userId).toRepresentation();
        } catch (Exception e) {
            log.error("User not found - UserId: {}, Error: {}", userId, e.getMessage());
            throw new KeycloakException("User not found: " + userId, e);
        }
    }

    public List<UserRepresentation> searchUsersByEmail(String email) {
        try {
            return getRealmResource().users().search(email, true);
        } catch (Exception e) {
            log.error("User search error - Email: {}, Error: {}", maskEmail(email), e.getMessage());
            return Collections.emptyList();
        }
    }

    @CacheEvict(value = "users", key = "#userId")
    public void updateUser(String userId, UserRepresentation user) {
        try {
            RealmResource realmResource = getRealmResource();
            realmResource.users().get(userId).update(user);
            log.info("User updated: {}", userId);

        } catch (Exception e) {
            log.error("User update error - UserId: {}, Error: {}", userId, e.getMessage());
            throw new KeycloakException("Failed to update user: " + e.getMessage(), e);
        }
    }

    public void sendVerificationEmail(String userId) {
        try {
            getRealmResource().users().get(userId).sendVerifyEmail();
            log.info("Verification email sent - UserId: {}", userId);

        } catch (Exception e) {
            log.error("Verification email error - UserId: {}, Error: {}",
                    userId, e.getMessage());
            throw new KeycloakException("Failed to send verification email: " + e.getMessage(), e);
        }
    }

    private RealmResource getRealmResource() {
        try {
            return keycloak.realm(realm);
        } catch (Exception e) {
            log.error("Realm resource error: {}", e.getMessage());
            throw new KeycloakException("Failed to get realm resource", e);
        }
    }

    // KeycloakAdminService.java

    public String getAdminToken() {
        try {
            return keycloak.tokenManager().getAccessTokenString();
        } catch (Exception e) {
            log.error("Failed to get admin token: {}", e.getMessage());
            throw new KeycloakException("Failed to get admin token", e);
        }
    }

    public String findOrCreateGoogleUser(GoogleUserInfo info) {
        try {
            // 1) E-mail ile ara
            List<UserRepresentation> existing = searchUsersByEmail(info.getEmail());
            if (!existing.isEmpty()) {
                // İlk eşleşenin id'si
                String userId = existing.get(0).getId();
                log.info("Google user exists - Email: {}, UserId: {}", maskEmail(info.getEmail()), userId);
                return userId;
            }

            // 2) Yoksa oluştur
            UserRepresentation user = new UserRepresentation();
            user.setEnabled(true);
            user.setUsername(info.getEmail());
            user.setEmail(info.getEmail());
            user.setFirstName(extractFirstName(info.getName()));
            user.setLastName(extractLastName(info.getName()));
            user.setEmailVerified(Boolean.TRUE); // Google e-postası doğrulanmış kabul

            // Şifre zorunlu değil; sosyal girişli kullanıcı olarak yaratıyoruz
            Map<String, List<String>> attrs = new HashMap<>();
            attrs.put("provider", Collections.singletonList("google"));
            if (info.getGoogleId() != null) {
                attrs.put("google_id", Collections.singletonList(info.getGoogleId()));
            }
            if (info.getPicture() != null) {
                attrs.put("picture", Collections.singletonList(info.getPicture()));
            }
            user.setAttributes(attrs);

            RealmResource realmResource = getRealmResource();
            UsersResource usersResource = realmResource.users();

            Response response = usersResource.create(user);
            try {
                if (response.getStatus() == 201) {
                    String userId = extractUserIdFromResponse(response);
                    log.info("Google user created - Email: {}, UserId: {}", maskEmail(info.getEmail()), userId);

                    // İstersen varsayılan rol ata (örn. USER)
                    try {
                        assignRoleToUser(userId, "USER");
                    } catch (Exception roleEx) {
                        log.warn("Role assignment for Google user failed: {}", roleEx.getMessage());
                    }

                    return userId;
                } else if (response.getStatus() == 409) {
                    // Yarış/race durumunda tekrar ara
                    List<UserRepresentation> after = searchUsersByEmail(info.getEmail());
                    if (!after.isEmpty()) {
                        return after.get(0).getId();
                    }
                    throw new KeycloakException("User already exists but cannot retrieve");
                } else {
                    String err = response.readEntity(String.class);
                    throw new KeycloakException("Could not create Google user: " + err);
                }
            } finally {
                response.close();
            }

        } catch (Exception e) {
            log.error("findOrCreateGoogleUser error - Email: {}, Err: {}", maskEmail(info.getEmail()), e.getMessage());
            throw new KeycloakException("findOrCreateGoogleUser failed: " + e.getMessage(), e);
        }
    }

    /** Ad-soyad basit bölücü (boş ise null güvenliği) */
    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "Google";
        String[] parts = fullName.trim().split("\\s+");
        return parts[0];
    }
    private String extractLastName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "User";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length <= 1) return "";
        return String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
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