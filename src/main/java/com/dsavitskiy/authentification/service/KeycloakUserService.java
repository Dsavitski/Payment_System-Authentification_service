package com.dsavitskiy.authentification.service;

import com.dsavitskiy.authentification.dto.RegisterRequestDto;
import com.dsavitskiy.authentification.exception.KeycloakRegistrationException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KeycloakUserService {

    private static final String USER_ROLE = "USER";

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    public UUID createKeycloakUser(RegisterRequestDto request) {

        UserRepresentation user = buildUserRepresentation(request);

        try (Response response = keycloak.realm(realm).users().create(user)) {
            validateResponse(response);
            UUID userId = extractUserId(response);
            assignRole(userId);
            return userId;
        }
    }

    public void deleteKeycloakUser(UUID userId) {
        keycloak.realm(realm).users().delete(userId.toString());
    }

    private UserRepresentation buildUserRepresentation(RegisterRequestDto request) {

        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.login());
        user.setFirstName(request.name());
        user.setLastName(request.surname());
        user.setEmail(request.email());
        user.setEnabled(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.password());
        credential.setTemporary(false);

        user.setCredentials(List.of(credential));
        return user;
    }

    private void validateResponse(Response response) {
        if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
            throw new KeycloakRegistrationException(
                "Failed to create user in Keycloak. Status: " + response.getStatus());
        }
    }

    private UUID extractUserId(Response response) {
        String location = response.getLocation().getPath();
        String id = location.substring(location.lastIndexOf('/') + 1);
        return UUID.fromString(id);
    }

    private void assignRole(UUID userId) {
        UserResource userResource = keycloak.realm(realm)
            .users()
            .get(userId.toString());
        RoleRepresentation role = keycloak.realm(realm)
            .roles()
            .get(USER_ROLE)
            .toRepresentation();
        userResource.roles()
            .realmLevel()
            .add(List.of(role));
    }
}