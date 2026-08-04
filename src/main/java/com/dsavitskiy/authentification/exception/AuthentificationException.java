package com.dsavitskiy.authentification.exception;

public class AuthentificationException extends RuntimeException {
    public AuthentificationException(String message) {
        super(message);
    }
    public AuthentificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
