package com.strux.user_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;

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
                // ✅ Worker kontrolü yap
                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                boolean isWorker = authorities.stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_WORKER") ||
                                a.getAuthority().equals("WORKER") ||
                                a.getAuthority().equals("SCOPE_WORKER"));

                // JWT'den company_id claim'ini al
                String companyId = jwt.getClaim("company_id");

                // ✅ Worker ise companyId opsiyonel (null olabilir)
                if (isWorker) {
                    log.debug("✅ Worker user - Company ID: {}", companyId != null ? companyId : "null (independent)");
                    return companyId;  // null olabilir, sorun yok
                }

                // ❌ Admin/Company user için companyId zorunlu
                if (companyId == null || companyId.isBlank()) {
                    log.error("❌ Company ID not found in JWT token for non-worker user");
                    log.debug("JWT claims: {}", jwt.getClaims());
                    throw new SecurityException("Company ID required for this role");
                }

                log.debug("✅ Company ID from JWT: {}", companyId);
                return companyId;
            }

            throw new SecurityException("Invalid authentication type");

        } catch (SecurityException e) {
            throw e;  // Re-throw SecurityException
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

    // ✅ Yeni helper metod ekle
    public boolean isWorker() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().contains("WORKER"));
        }
        return false;
    }
}