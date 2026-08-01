package com.dsavitskiy.authentification.service;

import com.dsavitskiy.authentification.dto.RegisterRequestDto;
import com.dsavitskiy.authentification.exception.KeycloakRegistrationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeycloakUserServiceTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private Response response;

    @Mock
    private UserResource userResource;

    @Mock
    private RolesResource rolesResource;

    @Mock
    private org.keycloak.admin.client.resource.RoleResource roleResource;

    @Mock
    private RoleMappingResource roleMappingResource;

    @Mock
    private RoleScopeResource roleScopeResource;

    @InjectMocks
    private KeycloakUserService keycloakUserService;

    private RegisterRequestDto request;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(
            keycloakUserService,
            "realm",
            "test-realm"
        );

        request = new RegisterRequestDto(
            "login",
            "password",
            "John",
            "Doe",
            LocalDate.of(2000, Month.JANUARY, 1),
            "john@test.com"
        );

        when(keycloak.realm("test-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
    }

    @Test
    void createKeycloakUser_shouldCreateUserSuccessfully() {

        UUID id = UUID.randomUUID();

        when(usersResource.create(any())).thenReturn(response);
        when(response.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
        when(response.getLocation()).thenReturn(URI.create("http://localhost/users/" + id));
        when(usersResource.get(id.toString())).thenReturn(userResource);
        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get("USER")).thenReturn(roleResource);

        RoleRepresentation role = new RoleRepresentation();

        when(roleResource.toRepresentation()).thenReturn(role);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

        assertDoesNotThrow(() -> keycloakUserService.createKeycloakUser(request));
        verify(usersResource).create(any());
        verify(roleScopeResource).add(any());
    }

    @Test
    void createKeycloakUser_shouldThrowWhenStatusIsNotCreated() {
        when(usersResource.create(any())).thenReturn(response);
        when(response.getStatus()).thenReturn(Response.Status.BAD_REQUEST.getStatusCode());
        assertThrows(KeycloakRegistrationException.class,
            () -> keycloakUserService.createKeycloakUser(request)
        );

        verify(usersResource).create(any());
    }
    @Test
    void deleteKeycloakUser_shouldDeleteUser() {

        UUID userId = UUID.randomUUID();

        assertDoesNotThrow(() -> keycloakUserService.deleteKeycloakUser(userId));

        verify(keycloak.realm("test-realm").users()).delete(userId.toString());
    }

    @Test
    void createKeycloakUser_shouldAssignUserRole() {

        UUID id = UUID.randomUUID();
        when(usersResource.create(any())).thenReturn(response);
        when(response.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
        when(response.getLocation()).thenReturn(URI.create("http://localhost/users/" + id));
        when(usersResource.get(id.toString())).thenReturn(userResource);
        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get("USER")).thenReturn(roleResource);

        RoleRepresentation role = new RoleRepresentation();
        role.setName("USER");

        when(roleResource.toRepresentation()).thenReturn(role);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

        keycloakUserService.createKeycloakUser(request);

        verify(roleScopeResource).add(argThat(list ->
                list.size() == 1 && "USER".equals(list.get(0).getName())));
    }

    @Test
    void createKeycloakUser_shouldExtractUserIdFromLocation() {
        UUID id = UUID.randomUUID();

        when(usersResource.create(any())).thenReturn(response);
        when(response.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
        when(response.getLocation()).thenReturn(URI.create("http://localhost/admin/realms/test/users/" + id));
        when(usersResource.get(id.toString())).thenReturn(userResource);
        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get("USER")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(new RoleRepresentation());
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

        UUID result = keycloakUserService.createKeycloakUser(request);

        assertEquals(id, result);
    }
}