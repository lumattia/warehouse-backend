package com.demo.warehouse.mapper;

import java.util.Set;
import java.util.UUID;

import com.demo.warehouse.domain.Tenant;
import com.demo.warehouse.domain.UserRole;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

public class UserDto {
    public static record LoggedUserDto(
        Long id,
        String username,
        String auth0Sub,
        UserRole role,
        Tenant tenant
    ) {}

    public static record UserCreateRequest(
        @NotBlank
        String username,
        @NotNull
        UserRole role,
        Set<UUID> allowedTenantIds
    ) {}

    public static record UserUpdateRequest(
        @NotNull
        Long id,
        @NotBlank
        String username,
        @NotNull
        UserRole role,
        Set<UUID> allowedTenantIds
    ) {}

    public static record UserFilterRequest(
        String username,
        UserRole role
    ) {}

    @Builder
    public static record UserResponse(
        Long id,
        String username,
        UserRole role,
        Tenant tenant,
        boolean isEditable
    ) {}
}