package com.dsavitskiy.authentification.config;

import com.dsavitskiy.authentification.client.decoder.KeycloakErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakFeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new KeycloakErrorDecoder();
    }
}