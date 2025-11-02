
package com.strux.project_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ============================================
                        // PUBLIC ENDPOINTS - Herkes Erişebilir
                        // ============================================

                        // Health checks
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/health/**").permitAll()

                        // Location endpoints (read-only)
                        .requestMatchers(HttpMethod.GET, "/api/locations/markers").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/locations/nearby").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/locations/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/locations/details/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/locations/projects/*/map-details").permitAll()

                        // Project endpoints (read-only)
                        .requestMatchers(HttpMethod.GET, "/api/projects").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/projects/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/projects/*/progress").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/projects/map").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/projects/company/*").permitAll()

                        // ============================================
                        // COMPANY_ADMIN - Şirket Yöneticisi
                        // ============================================

                        // Project CRUD
                        .requestMatchers(HttpMethod.POST, "/api/projects").hasRole("COMPANY_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/projects/*").hasRole("COMPANY_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/projects/*").hasRole("COMPANY_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/projects/*").hasRole("COMPANY_ADMIN")

                        // Location management
                        .requestMatchers(HttpMethod.POST, "/api/locations/projects/*").hasRole("COMPANY_ADMIN")

                        // Phase management
                        .requestMatchers(HttpMethod.POST, "/api/projects/*/phases").hasRole("COMPANY_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/projects/*/phases/*").hasRole("COMPANY_ADMIN")

                        // ============================================
                        // WORKER & COMPANY_ADMIN - Progress Update
                        // ============================================
                        .requestMatchers(HttpMethod.PATCH, "/api/projects/*/phases/*/progress")
                        .hasAnyRole("WORKER", "COMPANY_ADMIN")

                        // ============================================
                        // AUTHENTICATED - Filtering
                        // ============================================
                        .requestMatchers(HttpMethod.POST, "/api/locations/filter")
                        .hasAnyRole("HOMEOWNER", "COMPANY_ADMIN", "WORKER")

                        // Diğer tüm istekler authentication gerektirir
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return converter;
    }

    /**
     * Keycloak'tan gelen JWT token'daki rolleri Spring Security formatına çevirir
     * Keycloak: realm_access.roles = ["COMPANY_ADMIN"]
     * Spring: authorities = ["ROLE_COMPANY_ADMIN"]
     */
    static class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            // realm_access.roles'den role'leri al
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                List<String> roles = (List<String>) realmAccess.get("roles");
                for (String role : roles) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }
            }

            return authorities;
        }
    }
}