package com.strux.project_service.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class SecurityUtils {

    private final RestTemplate restTemplate;

    /**
     * JWT token'dan company_id claim'ini alır
     * HOMEOWNER için özel handling yapar - unit üzerinden company bulur
     */
    public String getCurrentUserCompanyId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            log.error("No valid JWT authentication found");
            throw new RuntimeException("User authentication not found");
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();

        // 1️⃣ Önce "company_id" claim'ini dene
        String companyId = jwt.getClaim("company_id");
        if (companyId == null) {
            companyId = jwt.getClaim("companyId");
        }

        if (companyId != null) {
            log.debug("✅ Retrieved company ID from JWT: {}", companyId);
            return companyId;
        }

        // 2️⃣ HOMEOWNER rolünü kontrol et
        Collection<String> roles = extractRoles(authentication);
        log.info("👤 User roles: {}", roles);

        if (roles.contains("ROLE_HOMEOWNER")) {
            log.info("🏠 HOMEOWNER detected - fetching company via unit assignment");
            return getCompanyIdForHomeowner(jwt);
        }

        // 3️⃣ Diğer roller için User Service'den al
        log.warn("⚠️ No company_id in JWT. Fetching from User Service...");
        return getCompanyIdFromUserService(jwt);
    }

    /**
     * HOMEOWNER için company ID'yi unit üzerinden bulur
     */
    private String getCompanyIdForHomeowner(Jwt jwt) {
        try {
            String userId = jwt.getSubject();
            String email = jwt.getClaim("email");

            log.info("📦 Fetching unit for homeowner: userId={}, email={}", userId, email);

            // Unit Service'den kullanıcının unit'ini al
            String unitServiceUrl = "http://localhost:9094/api/units/owner/" + userId;

            UnitResponse[] units = restTemplate.getForObject(unitServiceUrl, UnitResponse[].class);

            if (units == null || units.length == 0) {
                log.warn("⚠️ HOMEOWNER has no unit assigned yet - returning null");
                return null; // ✅ Unit yoksa null döndür (hata fırlatma)
            }

            String projectId = units[0].getProjectId();
            log.info("✅ Found unit with projectId: {}", projectId);

            // Project Service'den company ID al
            String projectServiceUrl = "http://localhost:9090/api/projects/" + projectId + "/company-id";
            String companyId = restTemplate.getForObject(projectServiceUrl, String.class);

            log.info("✅ Retrieved company ID for homeowner: {}", companyId);
            return companyId;

        } catch (Exception e) {
            log.error("❌ Failed to fetch company ID for homeowner: {}", e.getMessage());
            return null; // ✅ Hata olsa bile null döndür (exception fırlatma)
        }
    }

    /**
     * User Service'den company ID alır
     */
    private String getCompanyIdFromUserService(Jwt jwt) {
        try {
            String email = jwt.getClaim("email");
            if (email == null) {
                log.error("❌ No email found in JWT token!");
                throw new RuntimeException("Cannot determine company: no company_id or email in token");
            }

            log.info("📧 Fetching company ID for email: {}", email);
            String url = "http://localhost:9093/api/users/email/" + email;

            UserResponse user = restTemplate.getForObject(url, UserResponse.class);

            if (user == null || user.getCompanyId() == null) {
                log.error("❌ User not found or has no company assigned");
                throw new RuntimeException("User has no company assigned");
            }

            log.info("✅ Retrieved company ID from User Service: {}", user.getCompanyId());
            return user.getCompanyId();

        } catch (Exception e) {
            log.error("❌ Failed to fetch company ID from User Service: {}", e.getMessage());
            throw new RuntimeException("Failed to determine company ID", e);
        }
    }

    /**
     * JWT token'dan rolleri çıkarır
     */
    private Collection<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    /**
     * Kullanıcının HOMEOWNER rolü olup olmadığını kontrol eder
     */
    public boolean isHomeowner() {
        return hasRole("HOMEOWNER");
    }

    /**
     * JWT token'dan user ID'yi alır
     */
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            throw new RuntimeException("User authentication not found");
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getSubject();
    }

    /**
     * Kullanıcının rollerini kontrol eder
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    public boolean isCompanyAdmin() {
        return hasRole("COMPANY_ADMIN");
    }

    public boolean isWorker() {
        return hasRole("WORKER");
    }

    public void logTokenClaims() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            log.info("JWT Claims: {}", jwt.getClaims());
        }
    }

    // Inner classes for responses
    @lombok.Data
    private static class UserResponse {
        private String companyId;
        private String email;
    }

    @lombok.Data
    private static class UnitResponse {
        private String id;
        private String projectId;
        private String unitNumber;
    }
}