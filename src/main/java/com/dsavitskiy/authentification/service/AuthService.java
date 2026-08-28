package com.dsavitskiy.authentification.service;

import com.dsavitskiy.authentification.client.UserClient;
import com.dsavitskiy.authentification.dto.CreateUserRequestDto;
import com.dsavitskiy.authentification.dto.LoginRequestDto;
import com.dsavitskiy.authentification.dto.RefreshTokenRequestDto;
import com.dsavitskiy.authentification.dto.RegisterRequestDto;
import com.dsavitskiy.authentification.dto.TokenResponseDto;
import com.dsavitskiy.authentification.dto.UserResponseDto;
import com.dsavitskiy.authentification.exception.AuthentificationException;
import com.dsavitskiy.authentification.exception.CredentialException;
import com.dsavitskiy.authentification.exception.UserRegistrationException;
import com.dsavitskiy.authentification.mapper.AuthUserMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final KeycloakUserService keycloakUserService;
    private final AuthUserMapper authUserMapper;
    private final UserClient userClient;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${keycloak.server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    public UserResponseDto register(RegisterRequestDto registerRequestDto) {
        log.info("Registering user {}", registerRequestDto.email());
        UUID keycloakUserId = keycloakUserService.createKeycloakUser(registerRequestDto);
        CreateUserRequestDto createUserRequestDto = authUserMapper.toCreateUser(registerRequestDto, keycloakUserId);

        try {
            UserResponseDto userResponseDto = userClient.createUser(createUserRequestDto);
            log.info("User {} registered", createUserRequestDto.email());
            return userResponseDto;
        } catch (UserRegistrationException ex) {
            log.error("Registration failed! Rolling back keycloak user {}", keycloakUserId, ex);
            keycloakUserService.deleteKeycloakUser(keycloakUserId);
            throw ex;
        }
    }

    public TokenResponseDto login(LoginRequestDto request) {
        log.info("User {} is trying to login", request.login());
        String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("username", request.login());
        form.add("password", request.password());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(form, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, httpEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            try {
                return objectMapper.readValue(response.getBody(), TokenResponseDto.class);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse Keycloak JSON: {}", response.getBody(), e);
                throw new AuthentificationException("Failed to parse token response", e);
            }
        }
        
        throw new CredentialException("Invalid login or password");
    }

    public TokenResponseDto refreshToken(RefreshTokenRequestDto request) {
        log.info("Refreshing access token");
        String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("refresh_token", request.refreshToken());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(form, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, httpEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            try {
                return objectMapper.readValue(response.getBody(), TokenResponseDto.class);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse Keycloak JSON: {}", response.getBody(), e);
                throw new AuthentificationException("Failed to parse refresh token response", e);
            }
        }

        throw new CredentialException("Invalid refresh token");
    }
}