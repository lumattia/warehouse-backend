package com.demo.warehouse.mapper;

import java.time.Instant;
import java.util.Set;

import com.demo.warehouse.domain.ModuleType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

public class TenantDtos {
    
    public static record TenantCreateRequest(
        @NotBlank
        String name,
        @NotNull
        Set<ModuleType> modules
    ) {}

    public static record TenantUpdateRequest(
        @NotNull
        java.util.UUID id,
        @NotBlank
        String name,
        @NotNull
        Set<ModuleType> modules
    ) {}

    public static record TenantFilterRequest(
        String name,
        ModuleType module
    ) {}

    @Builder
    public static record TenantResponse(
        java.util.UUID id,
        String name,
        Instant createdAt,
        Instant expiresAt,
        Set<ModuleType> modules
    ) {}
}
