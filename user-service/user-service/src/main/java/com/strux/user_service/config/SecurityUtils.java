package com.strux.user_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SecurityUtils {

    public String getCurrentUserCompanyId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                throw new SecurityException("No authentication found");
            }

            if (authentication.getPrincipal() instanceof Jwt jwt) {
                // ✅ JWT'den company_id claim'ini al
                String companyId = jwt.getClaim("company_id");

                if (companyId == null || companyId.isBlank()) {
                    log.error("❌ Company ID not found in JWT token");
                    log.debug("JWT claims: {}", jwt.getClaims());
                    throw new SecurityException("Company ID not found in token");
                }

                log.debug("✅ Company ID from JWT: {}", companyId);
                return companyId;
            }

            throw new SecurityException("Invalid authentication type");

        } catch (Exception e) {
            log.error("❌ Error getting company ID: {}", e.getMessage());
            throw new SecurityException("Failed to get company ID", e);
        }
    }

    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject(); // Keycloak user ID
        }
        throw new SecurityException("User ID not found");
    }

    public String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("ROLE_UNKNOWN");
        }
        return "ROLE_UNKNOWN";
    }
}
