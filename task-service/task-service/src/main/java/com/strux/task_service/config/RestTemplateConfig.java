// RestTemplateConfig.java - Task Service

package com.strux.task_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {  // ✅ RestTemplate döndür
        return new RestTemplate();
    }
}