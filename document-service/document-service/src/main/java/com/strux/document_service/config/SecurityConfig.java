package com.strux.document_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();


        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:5174",
                "http://localhost:3000",
                "http://localhost:5175"
        ));

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        configuration.setAllowedHeaders(Arrays.asList("*"));

        configuration.setAllowCredentials(true);

        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "X-User-Id",
                "X-Device-Fingerprint"
        ));

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Health check endpoints
                        .requestMatchers(HttpMethod.GET, "/api/documents/health").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/health/**").permitAll()

                        // === DOCUMENT CONTROLLER ENDPOINTS ===

                        // Upload endpoints - Authenticated users
                        .requestMatchers(HttpMethod.POST, "/api/documents/upload").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/documents/upload-form").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/documents/bulk-upload").authenticated()

                        // Get document - All authenticated users can view
                        .requestMatchers(HttpMethod.GET, "/api/documents/{documentId}").authenticated()

                        // Download endpoints - Authenticated users
                        .requestMatchers(HttpMethod.GET, "/api/documents/{documentId}/download").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/documents/{documentId}/download-url").authenticated()

                        // Entity and company documents - Authenticated users
                        .requestMatchers(HttpMethod.GET, "/api/documents/entity/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/documents/company/**").authenticated()

                        // Search - Authenticated users
                        .requestMatchers(HttpMethod.POST, "/api/documents/search").authenticated()

                        // Update - Company Admin or Super Admin
                        .requestMatchers(HttpMethod.PUT, "/api/documents/{documentId}").hasAnyRole("COMPANY_ADMIN", "SUPER_ADMIN", "PROJECT_MANAGER")

                        // Delete - Company Admin or Super Admin
                        .requestMatchers(HttpMethod.DELETE, "/api/documents/{documentId}").hasAnyRole("COMPANY_ADMIN", "SUPER_ADMIN")

                        // Archive - Company Admin, Project Manager or Super Admin
                        .requestMatchers(HttpMethod.PUT, "/api/documents/{documentId}/archive").hasAnyRole("COMPANY_ADMIN", "SUPER_ADMIN", "PROJECT_MANAGER")

                        // Stats - Company Admin or Super Admin
                        .requestMatchers(HttpMethod.GET, "/api/documents/company/{companyId}/stats").hasAnyRole("COMPANY_ADMIN", "SUPER_ADMIN")

                        // === ENHANCED DOCUMENT CONTROLLER ENDPOINTS ===

                        // Progress upload - Workers, Foremen, Project Managers
                        .requestMatchers(HttpMethod.POST, "/api/documents/progress/upload").hasAnyRole("WORKER", "FOREMAN", "PROJECT_MANAGER", "COMPANY_ADMIN", "SUPER_ADMIN")

                        // Timeline - All authenticated users
                        .requestMatchers(HttpMethod.GET, "/api/documents/timeline/**").authenticated()

                        // Task comparison - All authenticated users
                        .requestMatchers(HttpMethod.GET, "/api/documents/task/{taskId}/comparison").authenticated()

                        // Phase documents - All authenticated users
                        .requestMatchers(HttpMethod.GET, "/api/documents/phase/**").authenticated()

                        // Pending approvals - Project Managers and above
                        .requestMatchers(HttpMethod.GET, "/api/documents/pending-approval").hasAnyRole("PROJECT_MANAGER", "COMPANY_ADMIN", "SUPER_ADMIN")

                        // Approve document - Project Managers and above
                        .requestMatchers(HttpMethod.PUT, "/api/documents/{documentId}/approve").hasAnyRole("PROJECT_MANAGER", "COMPANY_ADMIN", "SUPER_ADMIN")

                        // Worker documents - Workers can view their own, Managers can view all
                        .requestMatchers(HttpMethod.GET, "/api/documents/worker/**").hasAnyRole("WORKER", "FOREMAN", "PROJECT_MANAGER", "COMPANY_ADMIN", "SUPER_ADMIN")

                        // Homeowner view - Homeowners and above
                        .requestMatchers(HttpMethod.GET, "/api/documents/homeowner/**").hasAnyRole("HOMEOWNER", "PROJECT_MANAGER", "COMPANY_ADMIN", "SUPER_ADMIN")

                        // Project stats and progress - Project Managers and above
                        .requestMatchers(HttpMethod.GET, "/api/documents/project/{projectId}/stats").hasAnyRole("PROJECT_MANAGER", "COMPANY_ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/documents/project/{projectId}/progress").hasAnyRole("PROJECT_MANAGER", "COMPANY_ADMIN", "SUPER_ADMIN", "HOMEOWNER")

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

    static class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

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