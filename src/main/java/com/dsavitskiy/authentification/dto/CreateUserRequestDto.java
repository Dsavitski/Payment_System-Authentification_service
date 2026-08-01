package com.dsavitskiy.authentification.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CreateUserRequestDto(
    UUID id,
    String name,
    String surname,
    LocalDate birthDate,
    String email) {

}
