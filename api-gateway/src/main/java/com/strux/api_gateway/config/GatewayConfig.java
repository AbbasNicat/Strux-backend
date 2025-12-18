package com.strux.api_gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class GatewayConfig {

    @Bean
    public GlobalFilter customGlobalFilter() {
        return (exchange, chain) -> {
            String method = exchange.getRequest().getMethod().toString();
            String path = exchange.getRequest().getPath().toString();
            String uri = exchange.getRequest().getURI().toString();

            log.info("🌐 ========================================");
            log.info("📥 INCOMING REQUEST");
            log.info("   Method: {}", method);
            log.info("   Path: {}", path);
            log.info("   Full URI: {}", uri);
            log.info("🌐 ========================================");

            return chain.filter(exchange)
                    .then(Mono.fromRunnable(() -> {
                        int statusCode = exchange.getResponse().getStatusCode().value();
                        log.info("📤 OUTGOING RESPONSE");
                        log.info("   Status: {} {}", statusCode, exchange.getResponse().getStatusCode());
                        log.info("🌐 ========================================");
                    }));
        };
    }
}
