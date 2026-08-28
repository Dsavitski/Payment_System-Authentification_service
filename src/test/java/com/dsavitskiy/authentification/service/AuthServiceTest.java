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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuthService authService;

    private RegisterRequestDto registerRequest;
    private LoginRequestDto loginRequest;
    private RefreshTokenRequestDto refreshRequest;
    private UUID mockKeycloakUserId;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDto(
            "testuser",
            "Password123!",
            "John",
            "Doe",
            LocalDate.of(1990, Month.JANUARY, 1),
            "test@example.com"
        );
        loginRequest = new LoginRequestDto("testuser", "Password123!");
        refreshRequest = new RefreshTokenRequestDto("mock-refresh-token");
        mockKeycloakUserId = UUID.randomUUID();

        ReflectionTestUtils.setField(authService, "keycloakServerUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(authService, "realm", "payment-system");
        ReflectionTestUtils.setField(authService, "clientId", "test-client-id");
        ReflectionTestUtils.setField(authService, "clientSecret", "test-client-secret");
    }

    @Test
    void register_Successful() {
        CreateUserRequestDto createUserRequest = new CreateUserRequestDto(
            mockKeycloakUserId,
            "John",
            "Doe",
            LocalDate.of(1990, Month.JANUARY, 1),
            "test@example.com"
        );

        UserResponseDto expectedResponse = new UserResponseDto(
            mockKeycloakUserId,
            "John",
            "Doe",
            LocalDate.of(1990, Month.JANUARY, 1),
            "test@example.com",
            true
        );

        when(keycloakUserService.createKeycloakUser(registerRequest)).thenReturn(mockKeycloakUserId);
        when(authUserMapper.toCreateUser(registerRequest, mockKeycloakUserId)).thenReturn(createUserRequest);
        when(userClient.createUser(createUserRequest)).thenReturn(expectedResponse);

        UserResponseDto result = authService.register(registerRequest);

        assertNotNull(result);
        assertEquals(expectedResponse.email(), result.email());
        verify(keycloakUserService, times(1)).createKeycloakUser(registerRequest);
        verify(authUserMapper, times(1)).toCreateUser(registerRequest, mockKeycloakUserId);
        verify(userClient, times(1)).createUser(createUserRequest);
        verify(keycloakUserService, never()).deleteKeycloakUser(any());
    }

    @Test
    void register_RollbackOnUserClientFailure() {
        CreateUserRequestDto createUserRequest = new CreateUserRequestDto(
            mockKeycloakUserId,
            "John",
            "Doe",
            LocalDate.of(1990, Month.JANUARY, 1),
            "test@example.com"
        );

        when(keycloakUserService.createKeycloakUser(registerRequest)).thenReturn(mockKeycloakUserId);
        when(authUserMapper.toCreateUser(registerRequest, mockKeycloakUserId)).thenReturn(createUserRequest);
        when(userClient.createUser(createUserRequest)).thenThrow(new UserRegistrationException("DB Error"));

        UserRegistrationException exception = assertThrows(UserRegistrationException.class, () ->
            authService.register(registerRequest)
        );

        assertEquals("DB Error", exception.getMessage());
        verify(keycloakUserService, times(1)).createKeycloakUser(registerRequest);
        verify(userClient, times(1)).createUser(createUserRequest);
        verify(keycloakUserService, times(1)).deleteKeycloakUser(mockKeycloakUserId);
    }

    @Test
    void login_Successful() throws JsonProcessingException {
        String mockJsonResponse = "{\"access_token\":\"mock-access\",\"refresh_token\":\"mock-refresh\"}";
        TokenResponseDto expectedToken = new TokenResponseDto("mock-access", "mock-refresh");
        String expectedUrl = "http://localhost:8080/realms/payment-system/protocol/openid-connect/token";

        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>(mockJsonResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(eq(expectedUrl), any(HttpEntity.class), eq(String.class))).thenReturn(mockResponseEntity);
        when(objectMapper.readValue(eq(mockJsonResponse), any(Class.class))).thenReturn(expectedToken);

        TokenResponseDto result = authService.login(loginRequest);

        assertNotNull(result);
        assertEquals("mock-access", result.accessToken());
        verify(restTemplate, times(1)).postForEntity(eq(expectedUrl), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void login_FailsWithCredentialException_OnNon2xxStatus() throws JsonProcessingException {
        String expectedUrl = "http://localhost:8080/realms/payment-system/protocol/openid-connect/token";
        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED);

        when(restTemplate.postForEntity(eq(expectedUrl), any(HttpEntity.class), eq(String.class))).thenReturn(mockResponseEntity);

        CredentialException exception = assertThrows(CredentialException.class, () ->
            authService.login(loginRequest)
        );

        assertEquals("Invalid login or password", exception.getMessage());
        verify(objectMapper, never()).readValue(anyString(), any(Class.class));
    }

    @Test
    void login_FailsWithAuthentificationException_OnJsonParseError() throws JsonProcessingException {
        String mockJsonResponse = "{invalid json}";
        String expectedUrl = "http://localhost:8080/realms/payment-system/protocol/openid-connect/token";

        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>(mockJsonResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(eq(expectedUrl), any(HttpEntity.class), eq(String.class))).thenReturn(mockResponseEntity);
        when(objectMapper.readValue(eq(mockJsonResponse), any(Class.class))).thenThrow(new JsonProcessingException("Parse error") {});

        AuthentificationException exception = assertThrows(AuthentificationException.class, () ->
            authService.login(loginRequest)
        );

        assertTrue(exception.getMessage().contains("Failed to parse token response"));
    }

    @Test
    void refreshToken_Successful() throws JsonProcessingException {
        String mockJsonResponse = "{\"access_token\":\"new-access\",\"refresh_token\":\"new-refresh\"}";
        TokenResponseDto expectedToken = new TokenResponseDto("new-access", "new-refresh");
        String expectedUrl = "http://localhost:8080/realms/payment-system/protocol/openid-connect/token";

        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>(mockJsonResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(eq(expectedUrl), any(HttpEntity.class), eq(String.class))).thenReturn(mockResponseEntity);
        when(objectMapper.readValue(eq(mockJsonResponse), any(Class.class))).thenReturn(expectedToken);

        TokenResponseDto result = authService.refreshToken(refreshRequest);

        assertNotNull(result);
        assertEquals("new-access", result.accessToken());
        verify(restTemplate, times(1)).postForEntity(eq(expectedUrl), any(HttpEntity.class), eq(String.class));
    }
}