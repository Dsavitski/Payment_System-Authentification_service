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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private KeycloakUserService keycloakUserService;

    @Mock
    private AuthUserMapper authUserMapper;

    @Mock
    private UserClient userClient;

    @Mock
    private KeycloakClient keycloakClient;

    @InjectMocks
    private AuthService authService;

    private RegisterRequestDto registerRequest;
    private LoginRequestDto loginRequest;
    private RefreshTokenRequestDto refreshRequest;

    private UUID userId;

    private CreateUserRequestDto createUserRequest;

    private UserResponseDto userResponse;

    private TokenResponseDto tokenResponse;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(
            authService,
            "clientId",
            "test-client"
        );

        ReflectionTestUtils.setField(
            authService,
            "clientSecret",
            "secret"
        );

        userId = UUID.randomUUID();

        registerRequest = new RegisterRequestDto(
            "login",
            "password",
            "Ivan",
            "Ivanov",
            LocalDate.of(2000, Month.JANUARY,1),
            "ivan@test.com"
        );

        loginRequest = new LoginRequestDto(
            "login",
            "password"
        );

        refreshRequest = new RefreshTokenRequestDto(
            "refresh-token"
        );

        createUserRequest = new CreateUserRequestDto(
            userId,
            "Ivan",
            "Ivanov",
            LocalDate.of(2000,Month.JANUARY,1),
            "ivan@test.com"
        );

        userResponse = new UserResponseDto(
            userId,
            "Ivan",
            "Ivanov",
            LocalDate.of(2000,Month.JANUARY,1),
            "ivan@test.com",
            true
        );

        tokenResponse = new TokenResponseDto(
            "access-token",
            "refresh-token"
        );
    }

    @Test
    void register_shouldCreateUserSuccessfully() {

        when(keycloakUserService.createKeycloakUser(registerRequest))
            .thenReturn(userId);

        when(authUserMapper.toCreateUser(registerRequest, userId))
            .thenReturn(createUserRequest);

        when(userClient.createUser(createUserRequest))
            .thenReturn(userResponse);

        UserResponseDto result = authService.register(registerRequest);

        assertNotNull(result);
        assertEquals(userResponse, result);

        verify(keycloakUserService)
            .createKeycloakUser(registerRequest);

        verify(authUserMapper)
            .toCreateUser(registerRequest, userId);

        verify(userClient)
            .createUser(createUserRequest);

        verify(keycloakUserService, never())
            .deleteKeycloakUser(any());
    }

    @Test
    void register_shouldRollbackWhenUserServiceFails() {

        when(keycloakUserService.createKeycloakUser(registerRequest))
            .thenReturn(userId);

        when(authUserMapper.toCreateUser(registerRequest, userId))
            .thenReturn(createUserRequest);

        FeignException exception = mock(FeignException.class);

        when(userClient.createUser(createUserRequest))
            .thenThrow(exception);

        assertThrows(
            UserRegistrationException.class,
            () -> authService.register(registerRequest)
        );

        verify(keycloakUserService)
            .deleteKeycloakUser(userId);
    }

    @Test
    void login_shouldReturnTokens() {

        when(keycloakClient.getToken(
            ArgumentMatchers.any()))
            .thenReturn(tokenResponse);

        TokenResponseDto result =
            authService.login(loginRequest);

        assertNotNull(result);
        assertEquals(
            "access-token",
            result.accessToken()
        );

        assertEquals(
            "refresh-token",
            result.refreshToken()
        );

        verify(keycloakClient)
            .getToken(any());
    }

    @Test
    void login_shouldThrowCredentialExceptionWhenStatus400() {

        FeignException exception = mock(FeignException.class);

        when(exception.status()).thenReturn(400);

        when(keycloakClient.getToken(any()))
            .thenThrow(exception);

        assertThrows(
            CredentialException.class,
            () -> authService.login(loginRequest)
        );

        verify(keycloakClient)
            .getToken(any());
    }

    @Test
    void login_shouldThrowCredentialExceptionWhenStatus401() {

        FeignException exception = mock(FeignException.class);

        when(exception.status()).thenReturn(401);

        when(keycloakClient.getToken(any()))
            .thenThrow(exception);

        assertThrows(
            CredentialException.class,
            () -> authService.login(loginRequest)
        );

        verify(keycloakClient)
            .getToken(any());
    }
    @Test
    void login_shouldThrowAuthenticationExceptionWhenServerError() {

        FeignException exception = mock(FeignException.class);

        when(exception.status()).thenReturn(500);

        when(keycloakClient.getToken(any()))
            .thenThrow(exception);

        assertThrows(
            AuthentificationException.class,
            () -> authService.login(loginRequest)
        );

        verify(keycloakClient)
            .getToken(any());
    }

    @Test
    void refreshToken_shouldReturnNewTokens() {

        when(keycloakClient.getToken(any()))
            .thenReturn(tokenResponse);

        TokenResponseDto result =
            authService.refreshToken(refreshRequest);

        assertNotNull(result);

        assertEquals(
            "access-token",
            result.accessToken()
        );

        assertEquals(
            "refresh-token",
            result.refreshToken()
        );

        verify(keycloakClient)
            .getToken(any());
    }

    @Test
    void refreshToken_shouldThrowCredentialExceptionWhenStatus400() {

        FeignException exception = mock(FeignException.class);

        when(exception.status()).thenReturn(400);

        when(keycloakClient.getToken(any()))
            .thenThrow(exception);

        assertThrows(
            CredentialException.class,
            () -> authService.refreshToken(refreshRequest)
        );

        verify(keycloakClient)
            .getToken(any());
    }

    @Test
    void refreshToken_shouldThrowCredentialExceptionWhenStatus401() {

        FeignException exception = mock(FeignException.class);

        when(exception.status()).thenReturn(401);

        when(keycloakClient.getToken(any()))
            .thenThrow(exception);

        assertThrows(
            CredentialException.class,
            () -> authService.refreshToken(refreshRequest)
        );

        verify(keycloakClient)
            .getToken(any());
    }

    @Test
    void refreshToken_shouldThrowAuthenticationExceptionWhenServerError() {

        FeignException exception = mock(FeignException.class);

        when(exception.status()).thenReturn(500);

        when(keycloakClient.getToken(any()))
            .thenThrow(exception);

        assertThrows(
            AuthentificationException.class,
            () -> authService.refreshToken(refreshRequest)
        );

        verify(keycloakClient)
            .getToken(any());
    }
}