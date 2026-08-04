package com.dsavitskiy.authentification.service;

import com.dsavitskiy.authentification.client.KeycloakClient;
import com.dsavitskiy.authentification.client.UserClient;
import com.dsavitskiy.authentification.dto.CreateUserRequestDto;
import com.dsavitskiy.authentification.dto.LoginRequestDto;
import com.dsavitskiy.authentification.dto.RefreshTokenRequestDto;
import com.dsavitskiy.authentification.dto.RegisterRequestDto;
import com.dsavitskiy.authentification.dto.TokenResponseDto;
import com.dsavitskiy.authentification.dto.UserResponseDto;
import com.dsavitskiy.authentification.exception.UserRegistrationException;
import com.dsavitskiy.authentification.mapper.AuthUserMapper;
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

        CreateUserRequestDto createUserRequestDto =
            authUserMapper.toCreateUser(registerRequestDto, keycloakUserId);

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

        MultiValueMap<String, String> form = createBaseForm();
        form.add("grant_type", "password");
        form.add("username", request.login());
        form.add("password", request.password());

        TokenResponseDto token = keycloakClient.getToken(form);

        log.info("User {} logged in", request.login());
        return token;
    }

    public TokenResponseDto refreshToken(RefreshTokenRequestDto request) {
        log.info("Refreshing access token");

        MultiValueMap<String, String> form = createBaseForm();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", request.refreshToken());

        return keycloakClient.getToken(form);
    }

    private MultiValueMap<String, String> createBaseForm() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        return form;
    }


}
