package com.dsavitskiy.authentification.client.decoder;

import com.dsavitskiy.authentification.exception.UserRegistrationException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class UserClientErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        return new UserRegistrationException("Failed to create user");
    }
}