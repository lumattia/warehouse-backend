package com.demo.warehouse.mapper;

import java.util.Set;
import java.util.UUID;

import com.demo.warehouse.domain.Tenant;
import com.demo.warehouse.domain.UserRole;

public class UserDto {
    public static record LoggedUserDto(
        Long id,
        String username,
        String auth0Sub,
        UserRole role,
        Tenant tenant,
        Set<IdNameImpl<UUID>> allowedTenants
    ) {}
}