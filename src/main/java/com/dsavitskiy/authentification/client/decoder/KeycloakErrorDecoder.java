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
        try {
            String responseBody = response.body() == null ? "Empty response body" :
                Util.toString(response.body().asReader(Util.UTF_8));
            log.info("Keycloak returned error status: {} for method: {}. Body: {}",
                response.status(),
                methodKey,
                responseBody
            );

            if (response.status() == 400 || response.status() == 401) {
                return new CredentialException("Invalid login or password: " + responseBody);
            }

            return new AuthentificationException("Failed to process Keycloak request: " + responseBody);
        } catch (IOException e) {
            log.info("Failed to read Keycloak error response for method: {}", methodKey, e);

            return new AuthentificationException("Failed to process Keycloak error response", e);
        }
    }
}