package com.dsavitskiy.authentification.config;

import com.dsavitskiy.authentification.client.decoder.UserClientErrorDecoder;
import com.dsavitskiy.authentification.service.KeycloakTokenService;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;

@RequiredArgsConstructor
public class UserFeignConfig {

    private final KeycloakTokenService keycloakTokenService;

    @Bean
    public RequestInterceptor userClientRequestInterceptor() {
        return requestTemplate -> {
            String token = keycloakTokenService.getServiceAccountToken();
            requestTemplate.header(
                "Authorization",
                "Bearer " + token
            );
        };
    }

    @Bean
    public ErrorDecoder userClientErrorDecoder() {
        return new UserClientErrorDecoder();
    }
}
