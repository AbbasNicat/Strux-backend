package com.strux.task_service.config;

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

                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/health/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/tasks")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER")

                        .requestMatchers(HttpMethod.GET, "/api/tasks/*")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER", "HOMEOWNER")

                        // Get tasks by company
                        .requestMatchers(HttpMethod.GET, "/api/tasks/company/*")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER", "HOMEOWNER")

                        // Get tasks by project
                        .requestMatchers(HttpMethod.GET, "/api/tasks/project/*")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER", "HOMEOWNER")

                        // Get tasks by creator
                        .requestMatchers(HttpMethod.GET, "/api/tasks/creator/*")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER")

                        // Get tasks assigned to user
                        .requestMatchers(HttpMethod.GET, "/api/tasks/assigned/*")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER")

                        // Get tasks by status/priority/type
                        .requestMatchers(HttpMethod.GET, "/api/tasks/company/*/status/*")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER", "HOMEOWNER")
                        .requestMatchers(HttpMethod.GET, "/api/tasks/company/*/priority/*")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER", "HOMEOWNER")
                        .requestMatchers(HttpMethod.GET, "/api/tasks/company/*/type/*")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER", "HOMEOWNER")

                        // Get subtasks
                        .requestMatchers(HttpMethod.GET, "/api/tasks/*/subtasks")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER", "HOMEOWNER")

                        // Get tasks by asset/equipment/location
                        .requestMatchers(HttpMethod.GET, "/api/tasks/asset/*")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER")
                        .requestMatchers(HttpMethod.GET, "/api/tasks/equipment/*")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER")
                        .requestMatchers(HttpMethod.GET, "/api/tasks/location/*")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER")

                        // Get overdue/recurring/templates
                        .requestMatchers(HttpMethod.GET, "/api/tasks/company/*/overdue")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER")
                        .requestMatchers(HttpMethod.GET, "/api/tasks/company/*/recurring")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER")
                        .requestMatchers(HttpMethod.GET, "/api/tasks/company/*/templates")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER")

                        .requestMatchers(HttpMethod.GET, "/api/tasks/company/*/stats")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER", "HOMEOWNER")

                        .requestMatchers(HttpMethod.POST, "/api/tasks/search")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER", "HOMEOWNER")

                        .requestMatchers(HttpMethod.PUT, "/api/tasks/*")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER")

                        .requestMatchers(HttpMethod.PUT, "/api/tasks/*/assign")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER")

                        .requestMatchers(HttpMethod.PUT, "/api/tasks/*/progress")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER")

                        .requestMatchers(HttpMethod.PUT, "/api/tasks/*/complete")
                        .hasAnyRole("COMPANY_ADMIN", "WORKER")

                        .requestMatchers(HttpMethod.DELETE, "/api/tasks/*")
                        .hasRole("COMPANY_ADMIN")

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

