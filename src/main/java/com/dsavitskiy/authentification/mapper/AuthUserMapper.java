package com.dsavitskiy.authentification.mapper;

import com.dsavitskiy.authentification.dto.CreateUserRequestDto;
import com.dsavitskiy.authentification.dto.RegisterRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;


@Mapper(componentModel = "spring")
public interface AuthUserMapper {
    @Mapping(target = "id", source = "userId")
    CreateUserRequestDto toCreateUser(RegisterRequestDto registerRequestDto, UUID userId);
}
