package com.dsavitskiy.authentification.config;

import com.dsavitskiy.authentification.exception.AuthentificationException;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class FeignClientConfig {

    private final KeycloakProperties keycloakProperties;

    @Bean
    public RequestInterceptor feignTokenInterceptor() {
        return requestTemplate -> {
            String token = getServiceAccountToken();
            requestTemplate.header("Authorization", "Bearer " + token);
        };
    }

    private String getServiceAccountToken() {
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", keycloakProperties.getClientId());
        form.add("client_secret", keycloakProperties.getClientSecret());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
        String url = keycloakProperties.getServerUrl() + "/realms/" + keycloakProperties.getRealm() + "/protocol/openid-connect/token";

        Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

        if (response != null && response.containsKey("access_token")) {
            return (String) response.get("access_token");
        }
        throw new AuthentificationException("Failed to get token from Keycloak");
    }
}