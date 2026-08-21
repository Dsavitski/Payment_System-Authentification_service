package com.dsavitskiy.authentification.client.decoder;

import com.dsavitskiy.authentification.exception.AuthentificationException;
import com.dsavitskiy.authentification.exception.CredentialException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class KeycloakErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        String responseBody = "";
        try {
            if (response.body() != null) {
                responseBody = Util.toString(response.body().asReader(Util.UTF_8));
            }
        } catch (IOException e) {
            responseBody = "Failed to read response body";
        }

        log.error("Keycloak returned error status: {} for method: {}. Body: {}",
            response.status(), methodKey, responseBody);

        if (response.status() == 400 || response.status() == 401) {
            return new CredentialException("Invalid login or password. Keycloak says: " + responseBody);
        }

        return new AuthentificationException("Failed to login. Keycloak says: " + responseBody);
    }
}