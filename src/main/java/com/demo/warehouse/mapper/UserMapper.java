package com.demo.warehouse.mapper;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.demo.warehouse.domain.User;
import com.demo.warehouse.mapper.UserDto.LoggedUserDto;
import com.demo.warehouse.mapper.UserDto.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {
    LoggedUserDto toLogged(User entity);
    UserResponse toResponse(User entity);

    @Mapping(target = "allowedTenants", source = "allowedTenants", qualifiedByName = "tenantSetToIdNameSet")
    UserResponse toResponseWithTenants(User entity);

    @Named("tenantSetToIdNameSet")
    default Set<IdNameImpl<UUID>> tenantSetToIdNameSet(Set<com.demo.warehouse.domain.Tenant> tenants) {
        if (tenants == null) {
            return null;
        }
        return tenants.stream()
            .map(tenant -> {
                IdNameImpl<UUID> idName = new IdNameImpl<>();
                idName.setId(tenant.getId());
                idName.setName(tenant.getName());
                return idName;
            })
            .collect(Collectors.toSet());
    }
}