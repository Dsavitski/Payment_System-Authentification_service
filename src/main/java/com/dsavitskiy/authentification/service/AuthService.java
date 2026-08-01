package com.dsavitskiy.authentification.service;

import com.dsavitskiy.authentification.client.KeycloakClient;
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
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final KeycloakUserService keycloakUserService;
    private final AuthUserMapper authUserMapper;
    private final UserClient userClient;
    private final KeycloakClient keycloakClient;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;


    public UserResponseDto register(RegisterRequestDto registerRequestDto) {
        log.info("Registering user {}", registerRequestDto.email());
        UUID keycloakUserId = keycloakUserService.createKeycloakUser(registerRequestDto);
        try {
            CreateUserRequestDto createUserRequestDto = authUserMapper.
                toCreateUser(registerRequestDto, keycloakUserId);
            UserResponseDto userResponseDto = userClient.createUser(createUserRequestDto);
            log.info("User {} registered", createUserRequestDto.email());
            return userResponseDto;
        } catch (FeignException ex) {
            log.error("Registration failed! Rolling back keycloak user {} ", keycloakUserId, ex);
            keycloakUserService.deleteKeycloakUser(keycloakUserId);
            throw new UserRegistrationException("Failed to create user", ex);
        }
    }

    public TokenResponseDto login(LoginRequestDto request) {
        log.info("User {} is trying to login", request.login());
        try {
            MultiValueMap<String, String> form = createBaseForm();
            form.add("grant_type", "password");
            form.add("username", request.login());
            form.add("password", request.password());

            TokenResponseDto token = keycloakClient.getToken(form);
            log.info("User {} logged in", request.login());
            return token;

        } catch (FeignException ex) {
            if (ex.status() == 400 || ex.status() == 401) {
                log.warn("Invalid credentials for {}", request.login());
                throw new CredentialException("Invalid login or password");
            }
            log.error("Login failed", ex);
            throw new AuthentificationException("Failed to login", ex);
        }

    }

    public TokenResponseDto refreshToken(RefreshTokenRequestDto request) {
        log.info("Refreshing access token");
        try {
            MultiValueMap<String, String> form = createBaseForm();

            form.add("grant_type", "refresh_token");
            form.add("refresh_token", request.refreshToken());

            return keycloakClient.getToken(form);

        } catch (FeignException ex) {

            if (ex.status() == 400 || ex.status() == 401) {
                log.warn("Invalid refresh token");
                throw new CredentialException("Refresh token is invalid");
            }

            log.error("Refresh token failed", ex);
            throw new AuthentificationException("Failed to refresh token", ex);
        }
    }

    private MultiValueMap<String, String> createBaseForm() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        return form;
    }


}
