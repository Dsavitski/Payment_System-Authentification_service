package com.dsavitskiy.authentification.config;

import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class KeycloakConfig {

    private final KeycloakProperties keycloakProperties;

    @Bean(destroyMethod = "close")
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
            .serverUrl(keycloakProperties.getServerUrl())
            .realm(keycloakProperties.getRealm())
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .clientId(keycloakProperties.getClientId())
            .clientSecret(keycloakProperties.getClientSecret())
            .build();
    }
}