package com.dsavitskiy.authentification.dto;

public record TokenResponseDto(
    String accessToken,
    String refreshToken) {
}
