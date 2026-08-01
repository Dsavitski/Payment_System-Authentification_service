package com.dsavitskiy.authentification.controller;

import com.dsavitskiy.authentification.dto.LoginRequestDto;
import com.dsavitskiy.authentification.dto.RefreshTokenRequestDto;
import com.dsavitskiy.authentification.dto.RegisterRequestDto;
import com.dsavitskiy.authentification.dto.TokenResponseDto;
import com.dsavitskiy.authentification.dto.UserResponseDto;
import com.dsavitskiy.authentification.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(
        @Valid @RequestBody RegisterRequestDto request) {

        UserResponseDto response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(
        @Valid @RequestBody LoginRequestDto request) {

        TokenResponseDto response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refreshToken(
        @Valid @RequestBody RefreshTokenRequestDto request) {
        TokenResponseDto response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }
}