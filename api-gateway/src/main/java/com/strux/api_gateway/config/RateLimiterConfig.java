package com.strux.api_gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    /**
     * IP-based rate limiting
     * Her IP üçün ayrıca rate limit
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest()
                    .getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";

            return Mono.just(ip);
        };
    }

    /**
     * User-based rate limiting (JWT token varsa)
     * Token yoxdursa IP istifadə edir
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // Authorization header-dən user ID çıxart
            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    // Token-dan user ID çıxarmaq (simplified)
                    String token = authHeader.substring(7);
                    // JWT parse edib userId çıxara bilərsən
                    // Amma bu halda Gateway-də JWT dependency lazımdır

                    // Fallback: IP istifadə et
                    String ip = exchange.getRequest()
                            .getRemoteAddress() != null
                            ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                            : "unknown";

                    return Mono.just(ip);
                } catch (Exception e) {
                    return Mono.just("unknown");
                }
            }

            // Token yoxdursa IP istifadə et
            String ip = exchange.getRequest()
                    .getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";

            return Mono.just(ip);
        };
    }

    /**
     * Path-based rate limiting
     * Hər endpoint üçün ayrıca rate limit
     */
    @Bean
    public KeyResolver pathKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getPath().value());
    }
}