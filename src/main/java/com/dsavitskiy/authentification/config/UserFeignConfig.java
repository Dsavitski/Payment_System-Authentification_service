package com.dsavitskiy.authentification.config;

import com.dsavitskiy.authentification.client.decoder.UserClientErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class UserFeignConfig {
    @Bean
    public ErrorDecoder userClientErrorDecoder() {
        return new UserClientErrorDecoder();
    }
}
