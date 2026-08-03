package com.dsavitskiy.authentification.client.decoder;

import com.dsavitskiy.authentification.exception.AuthentificationException;
import com.dsavitskiy.authentification.exception.CredentialException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class KeycloakErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {

        if (response.status() == 400 || response.status() == 401) {
            return new CredentialException("Invalid login or password");
        }

        return new AuthentificationException("Failed to login");
    }
}
