package com.dsavitskiy.authentification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record RegisterRequestDto(
    @NotBlank(message = "Login is obligatory")
    String login,
    @NotBlank(message = "Password is obligatory")
    String password,
    @NotBlank(message = "Name is obligatory")
    String name,
    @NotBlank(message = "Surname is obligatory")
    String surname,
    @Past(message = "Birth date must be in the past")
    LocalDate birthDate,
    @Email(message = "Email is invalid")
    @NotBlank(message = "Email is obligatory")
    String email) {

}
