package com.dsavitskiy.authentification.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDto(
    @NotBlank
    String refreshToken
) {

}
