package com.dsavitskiy.authentification.dto;

public record KeycloakTokenRequest(
    String grant_type,
    String client_id,
    String client_secret,
    String username,
    String password,
    String refresh_token
) {}