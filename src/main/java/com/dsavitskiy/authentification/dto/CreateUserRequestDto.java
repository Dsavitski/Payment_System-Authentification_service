package com.dsavitskiy.authentification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;
import java.util.UUID;

public record CreateUserRequestDto(
    @NotNull(message = "Id is obligatory")
    UUID id,
    @NotBlank(message = "Name is obligatory")
    String name,
    @NotBlank(message = "Surname is obligatory")
    String surname,
    @NotNull(message = "Birth date is obligatory")
    @Past(message = "Birth date must be in past")
    LocalDate birthDate,
    @NotBlank
    @Email(message = "Invalid email")
    String email) {

}
