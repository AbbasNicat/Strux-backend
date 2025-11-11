package com.strux.project_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * JWT token'dan kullanıcı bilgilerini almak için utility sınıfı
 */
@Component
@Slf4j
public class SecurityUtils {

    /**
     * JWT token'dan company_id claim'ini alır
     * @return Company ID
     * @throws RuntimeException Eğer authentication bulunamazsa veya company_id yoksa
     */
    public String getCurrentUserCompanyId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            log.error("No valid JWT authentication found");
            throw new RuntimeException("User authentication not found");
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();

        // Önce "company_id" claim'ini dene
        String companyId = jwt.getClaim("company_id");

        // Eğer yoksa "companyId" claim'ini dene (camelCase)
        if (companyId == null) {
            companyId = jwt.getClaim("companyId");
        }

        if (companyId == null) {
            log.error("No company_id found in JWT token. Available claims: {}", jwt.getClaims().keySet());
            throw new RuntimeException("Company ID not found in token");
        }

        log.debug("Retrieved company ID from JWT: {}", companyId);
        return companyId;
    }

    /**
     * JWT token'dan user ID'yi alır
     * @return User ID (subject)
     */
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            throw new RuntimeException("User authentication not found");
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getSubject(); // "sub" claim'i
    }

    /**
     * Kullanıcının rollerini kontrol eder
     * @param role Kontrol edilecek rol (örn: "COMPANY_ADMIN")
     * @return Rol varsa true
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    /**
     * Kullanıcının COMPANY_ADMIN rolü olup olmadığını kontrol eder
     */
    public boolean isCompanyAdmin() {
        return hasRole("COMPANY_ADMIN");
    }

    /**
     * Kullanıcının WORKER rolü olup olmadığını kontrol eder
     */
    public boolean isWorker() {
        return hasRole("WORKER");
    }

    /**
     * JWT token'ın tüm claim'lerini loglar (debug için)
     */
    public void logTokenClaims() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            log.info("JWT Claims: {}", jwt.getClaims());
        }
    }
}