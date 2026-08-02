package com.dsavitskiy.authentification.client;

import com.dsavitskiy.authentification.config.FeignClientConfig;
import com.dsavitskiy.authentification.dto.CreateUserRequestDto;
import com.dsavitskiy.authentification.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service",
             url = "${services.user-service.url}",
             configuration = FeignClientConfig.class)
public interface UserClient {
    @PostMapping("/api/users")
    UserResponseDto createUser(@RequestBody CreateUserRequestDto createUserRequestDto);
}
