package com.dsavitskiy.authentification.exception;

import com.dsavitskiy.authentification.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthentificationException.class)
    public ResponseEntity<ErrorResponseDto> authenticationException(AuthentificationException ex) {
        log.info("Authentication exception: {}", ex.getMessage());
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponseDto> conflictException(ConflictException ex) {
        log.info("Conflict exception: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(CredentialException.class)
    public ResponseEntity<ErrorResponseDto> credentialException(CredentialException ex) {
        log.info("Credential exception: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(KeycloakRegistrationException.class)
    public ResponseEntity<ErrorResponseDto> keycloakRegistrationException(KeycloakRegistrationException ex) {
        log.info("Keycloak registration exception: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    @ExceptionHandler(UserRegistrationException.class)
    public ResponseEntity<ErrorResponseDto> userRegistrationException(UserRegistrationException ex) {
        log.info("User registration exception: {}", ex.getMessage());
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnexpectedException(Exception ex) {
        log.info("Unexpected exception: {}", ex.getMessage());
        return buildResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Unexpected server error: " + ex.getMessage()
        );
    }

    private ResponseEntity<ErrorResponseDto> buildResponse(HttpStatus status, String message) {
        ErrorResponseDto error = new ErrorResponseDto(
            LocalDateTime.now(ZoneId.of("Europe/Minsk")),
            status.value(),
            status.getReasonPhrase(),
            message
        );
        return ResponseEntity.status(status).body(error);
    }
}