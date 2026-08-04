package com.dsavitskiy.authentification.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
    @NotBlank(message = "Login can't be empty")
    String login,
    @NotBlank(message = "Password can't be empty")
    String password) {
}
