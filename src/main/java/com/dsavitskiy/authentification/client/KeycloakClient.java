package com.dsavitskiy.authentification.client;

import com.dsavitskiy.authentification.dto.TokenResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "keycloak-client",
    url = "${keycloak.server-url}/realms/${keycloak.realm}/protocol/openid-connect",
    configuration = KeycloakClient.class)
public interface KeycloakClient {

    @PostMapping(
        value = "/token",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    TokenResponseDto getToken(
        @RequestBody MultiValueMap<String, String> form
    );
}
