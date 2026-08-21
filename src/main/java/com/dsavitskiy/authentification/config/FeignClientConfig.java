package com.dsavitskiy.authentification.config;

import com.dsavitskiy.authentification.service.KeycloakTokenService;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FeignClientConfig {

    private final KeycloakTokenService keycloakTokenService;

    @Bean
    public RequestInterceptor feignTokenInterceptor() {
        return requestTemplate -> {
            String token = keycloakTokenService.getServiceAccountToken();
            requestTemplate.header("Authorization", "Bearer " + token);
        };
    }
}