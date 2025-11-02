package com.strux.issue_service.config;

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
                        // Health check endpoints
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/health/**").permitAll()

                        // Create issue - Workers and Homeowners can report issues
                        .requestMatchers(HttpMethod.POST, "/api/issues")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER", "HOMEOWNER")

                        // Get single issue - Everyone can view
                        .requestMatchers(HttpMethod.GET, "/api/issues/*")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER", "HOMEOWNER")

                        // Get issues by company
                        .requestMatchers(HttpMethod.GET, "/api/issues/company/*")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER", "HOMEOWNER")

                        // Get issues by user (reporter)
                        .requestMatchers(HttpMethod.GET, "/api/issues/user/*")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER", "HOMEOWNER")

                        // Get issues assigned to user
                        .requestMatchers(HttpMethod.GET, "/api/issues/assigned/*")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER")

                        // Get issues by status
                        .requestMatchers(HttpMethod.GET, "/api/issues/company/*/status/*")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER", "HOMEOWNER")

                        // Get issues by project
                        .requestMatchers(HttpMethod.GET, "/api/issues/project/*")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER", "HOMEOWNER")

                        // Get issues by task
                        .requestMatchers(HttpMethod.GET, "/api/issues/task/*")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER", "HOMEOWNER")

                        // Get issues by asset
                        .requestMatchers(HttpMethod.GET, "/api/issues/asset/*")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER")

                        // Get overdue issues
                        .requestMatchers(HttpMethod.GET, "/api/issues/company/*/overdue")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER")

                        // Search issues
                        .requestMatchers(HttpMethod.POST, "/api/issues/search")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER", "HOMEOWNER")

                        // Update issue - Only admins and workers
                        .requestMatchers(HttpMethod.PUT, "/api/issues/*")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER")

                        // Assign issue - Only admins
                        .requestMatchers(HttpMethod.PUT, "/api/issues/*/assign")
                        .hasRole("COMPANY_ADMIN")

                        // Resolve issue - Admins and workers
                        .requestMatchers(HttpMethod.PUT, "/api/issues/*/resolve")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER")

                        // Close issue - Admins and workers
                        .requestMatchers(HttpMethod.PUT, "/api/issues/*/close")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER")

                        // Reopen issue - Everyone can reopen
                        .requestMatchers(HttpMethod.PUT, "/api/issues/*/reopen")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER", "HOMEOWNER")

                        // Delete issue - Only admins
                        .requestMatchers(HttpMethod.DELETE, "/api/issues/*")
                        .hasRole("COMPANY_ADMIN")

                        // Get issue statistics
                        .requestMatchers(HttpMethod.GET, "/api/issues/company/*/stats")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER", "HOMEOWNER")

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