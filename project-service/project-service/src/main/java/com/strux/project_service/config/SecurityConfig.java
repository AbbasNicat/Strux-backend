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
                "http://localhost:5175",
                "https://struxtech.app",
                "https://www.struxtech.app",
                "https://admin.struxtech.app",
                "https://app.strux.az",
                "https://api.struxtech.app"
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
                        // ============================================
                        // PUBLIC ENDPOINTS - EN BAŞTA OLMALI
                        // ============================================

                        // Health checks
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/health/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ TÜM Location search endpoints - ÖNCE BUNLAR
                        .requestMatchers("/api/locations/search").permitAll()
                        .requestMatchers("/api/locations/details/**").permitAll()
                        .requestMatchers("/api/locations/reverse-geocode").permitAll() // ✅ EN SPESIFIK OLANI EN ÜSTTE

                        // ============================================
                        // AUTHENTICATED - Location endpoints
                        // ============================================
                        .requestMatchers(HttpMethod.GET, "/api/locations/markers").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/locations/nearby").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/locations/projects/*/map-details").authenticated()

                        // Project endpoints - Read (company bazlı)
                        .requestMatchers(HttpMethod.GET, "/api/projects").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/projects/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/projects/*/progress").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/projects/map").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/projects/company/*").authenticated()

                        // ============================================
                        // COMPANY_ADMIN - Şirket Yöneticisi
                        // ============================================
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
                        .requestMatchers(HttpMethod.POST, "/api/locations/filter").authenticated()

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