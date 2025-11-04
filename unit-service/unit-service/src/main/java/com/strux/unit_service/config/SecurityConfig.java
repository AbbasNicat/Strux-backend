package com.strux.unit_service.config;

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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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

                        // Health & Actuator endpoints
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/health/**").permitAll()

                        // ============ CREATE UNIT ============
                        .requestMatchers(HttpMethod.POST, "/api/units")
                        .hasAnyRole("COMPANY_ADMIN", "PROJECT_MANAGER")

                        // ============ GET SINGLE UNIT ============
                        .requestMatchers(HttpMethod.GET, "/api/units/*")
                        .hasAnyRole("COMPANY_ADMIN", "PROJECT_MANAGER", "WORKER", "HOMEOWNER")

                        // ============ GET UNITS BY PROJECT ============
                        .requestMatchers(HttpMethod.GET, "/api/units/project/*")
                        .hasAnyRole("COMPANY_ADMIN", "PROJECT_MANAGER", "WORKER", "HOMEOWNER")

                        // ============ GET UNITS BY BUILDING ============
                        .requestMatchers(HttpMethod.GET, "/api/units/building/*")
                        .hasAnyRole("COMPANY_ADMIN", "PROJECT_MANAGER", "WORKER")

                        // ============ GET UNITS BY BLOCK ============
                        .requestMatchers(HttpMethod.GET, "/api/units/project/*/block/*")
                        .hasAnyRole("COMPANY_ADMIN", "PROJECT_MANAGER", "WORKER")

                        // ============ GET UNITS BY FLOOR ============
                        .requestMatchers(HttpMethod.GET, "/api/units/project/*/floor/*")
                        .hasAnyRole("COMPANY_ADMIN", "PROJECT_MANAGER", "WORKER")

                        // ============ GET UNITS BY STATUS ============
                        .requestMatchers(HttpMethod.GET, "/api/units/project/*/status/*")
                        .hasAnyRole("COMPANY_ADMIN", "PROJECT_MANAGER", "WORKER")

                        // ============ GET UNITS BY SALE STATUS ============
                        .requestMatchers(HttpMethod.GET, "/api/units/project/*/sale-status/*")
                        .hasAnyRole("COMPANY_ADMIN", "PROJECT_MANAGER", "SALES")

                        // ============ GET UNITS BY TYPE ============
                        .requestMatchers(HttpMethod.GET, "/api/units/project/*/type/*")
                        .hasAnyRole("COMPANY_ADMIN", "PROJECT_MANAGER", "WORKER", "SALES")

                        // ============ GET UNITS BY OWNER ============
                        .requestMatchers(HttpMethod.GET, "/api/units/owner/*")
                        .hasAnyRole("COMPANY_ADMIN", "PROJECT_MANAGER", "HOMEOWNER")

                        // ============ GET AVAILABLE UNITS ============
                        .requestMatchers(HttpMethod.GET, "/api/units/project/*/available")
                        .hasAnyRole("COMPANY_ADMIN", "PROJECT_MANAGER", "SALES", "HOMEOWNER")

                        // ============ GET OVERDUE UNITS ============
                        .requestMatchers(HttpMethod.GET, "/api/units/project/*/overdue")
                        .hasAnyRole("COMPANY_ADMIN", "PROJECT_MANAGER")

                        // ============ SEARCH UNITS ============
                        .requestMatchers(HttpMethod.POST, "/api/units/search")
                        .hasAnyRole("COMPANY_ADMIN", "PROJECT_MANAGER", "WORKER", "SALES", "HOMEOWNER")

                        // ============ UPDATE UNIT ============
                        .requestMatchers(HttpMethod.PUT, "/api/units/*")
                        .hasAnyRole("COMPANY_ADMIN", "PROJECT_MANAGER")

                        // ============ UPDATE PROGRESS ============
                        .requestMatchers(HttpMethod.PUT, "/api/units/*/progress")
                        .hasAnyRole("COMPANY_ADMIN", "PROJECT_MANAGER", "WORKER")

                        // ============ RESERVE UNIT ============
                        .requestMatchers(HttpMethod.PUT, "/api/units/*/reserve")
                        .hasAnyRole("COMPANY_ADMIN", "PROJECT_MANAGER", "SALES")

                        // ============ SELL UNIT ============
                        .requestMatchers(HttpMethod.PUT, "/api/units/*/sell")
                        .hasAnyRole("COMPANY_ADMIN", "PROJECT_MANAGER", "SALES")

                        // ============ CANCEL RESERVATION ============
                        .requestMatchers(HttpMethod.PUT, "/api/units/*/cancel-reservation")
                        .hasAnyRole("COMPANY_ADMIN", "PROJECT_MANAGER", "SALES")

                        // ============ DELETE UNIT ============
                        .requestMatchers(HttpMethod.DELETE, "/api/units/*")
                        .hasRole("COMPANY_ADMIN")

                        // ============ WORK ITEMS - CREATE ============
                        .requestMatchers(HttpMethod.POST, "/api/units/*/work-items")
                        .hasAnyRole("COMPANY_ADMIN", "PROJECT_MANAGER", "WORKER")

                        // ============ WORK ITEMS - GET ============
                        .requestMatchers(HttpMethod.GET, "/api/units/*/work-items")
                        .hasAnyRole("COMPANY_ADMIN", "PROJECT_MANAGER", "WORKER", "HOMEOWNER")

                        // ============ WORK ITEMS - UPDATE ============
                        .requestMatchers(HttpMethod.PUT, "/api/units/work-items/*")
                        .hasAnyRole("COMPANY_ADMIN", "PROJECT_MANAGER", "WORKER")

                        // ============ WORK ITEMS - DELETE ============
                        .requestMatchers(HttpMethod.DELETE, "/api/units/work-items/*")
                        .hasAnyRole("COMPANY_ADMIN", "PROJECT_MANAGER")

                        // ============ STATISTICS ============
                        .requestMatchers(HttpMethod.GET, "/api/units/project/*/stats")
                        .hasAnyRole("COMPANY_ADMIN", "PROJECT_MANAGER", "SALES")

                        // All other requests must be authenticated
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

            // realm_access.roles'den role'leri al
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                List<String> roles = (List<String>) realmAccess.get("roles");
                for (String role : roles) {
                    // Spring Security için ROLE_ prefix'i ekle
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }
            }

            // resource_access'den client-specific roller (opsiyonel)
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess != null) {
                for (Map.Entry<String, Object> entry : resourceAccess.entrySet()) {
                    if (entry.getValue() instanceof Map) {
                        Map<String, Object> resource = (Map<String, Object>) entry.getValue();
                        if (resource.containsKey("roles")) {
                            List<String> clientRoles = (List<String>) resource.get("roles");
                            for (String role : clientRoles) {
                                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                            }
                        }
                    }
                }
            }

            return authorities;
        }
    }
}