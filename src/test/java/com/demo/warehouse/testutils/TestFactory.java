package com.demo.warehouse.testutils;

import com.demo.warehouse.domain.Dress;
import com.demo.warehouse.domain.DressMovement;
import com.demo.warehouse.domain.ModuleType;
import com.demo.warehouse.domain.Tenant;
import com.demo.warehouse.domain.User;
import com.demo.warehouse.domain.UserRole;
import com.demo.warehouse.mapper.DressDtos;
import com.demo.warehouse.mapper.TenantDtos;
import com.demo.warehouse.mapper.UserDto;
import com.demo.warehouse.tenantFilter.UserContext;
import com.demo.warehouse.tenantFilter.UserContextHolder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TestFactory {

    public static Dress createDress(Long id, String title, String sku, String size, String color, int stock, BigDecimal price) {
        Dress dress = new Dress();
        dress.setId(id);
        dress.setTitle(title);
        dress.setSku(sku);
        dress.setSize(size);
        dress.setColor(color);
        dress.setStock(stock);
        dress.setPrice(price);
        return dress;
    }

    public static Dress createDefaultDress() {
        return createDress(1L, "Test Dress", "SKU001", "M", "#FF0000", 10, new BigDecimal("100.00"));
    }

    public static DressDtos.DressResponse createDressResponse(Long id, String title, String sku, String size, String color, BigDecimal price, int stock) {
        return DressDtos.DressResponse.builder()
                .id(id)
                .title(title)
                .sku(sku)
                .size(size)
                .color(color)
                .price(price)
                .stock(stock)
                .build();
    }

    public static DressDtos.DressResponse createDefaultDressResponse() {
        return createDressResponse(1L, "Test Dress", "SKU001", "M", "#FF0000", new BigDecimal("100.00"), 10);
    }

    public static DressMovement createDressMovement(Long id, Dress dress, int quantity, Instant instant) {
        DressMovement movement = new DressMovement();
        movement.setId(id);
        movement.setDress(dress);
        movement.setQuantity(quantity);
        movement.setInstant(instant != null ? instant : Instant.now());
        return movement;
    }

    public static DressMovement createDefaultDressMovement(Dress dress) {
        return createDressMovement(1L, dress, 5, Instant.now());
    }

    public static Tenant createTenant(UUID id, String name, Set<ModuleType> modules) {
        Tenant tenant = new Tenant();
        tenant.setId(id != null ? id : UUID.randomUUID());
        tenant.setName(name);
        tenant.setModules(modules);
        return tenant;
    }

    public static Tenant createDefaultTenant() {
        return createTenant(UUID.randomUUID(), "Test Tenant", Set.of(ModuleType.DRESS));
    }

    public static TenantDtos.TenantResponse createTenantResponse(UUID id, String name, Set<ModuleType> modules) {
        return TenantDtos.TenantResponse.builder()
                .id(id)
                .name(name)
                .modules(modules)
                .createdAt(Instant.now())
                .expiresAt(null)
                .build();
    }

    public static TenantDtos.TenantResponse createDefaultTenantResponse(UUID id) {
        return createTenantResponse(id, "Test Tenant", Set.of(ModuleType.DRESS));
    }

    public static User createUser(Long id, String username, String auth0Sub, UserRole role, Tenant tenant, Set<Tenant> allowedTenants) {
        User user = new User(id, username, auth0Sub, role, allowedTenants != null ? allowedTenants : new HashSet<>());
        user.setTenant(tenant);
        return user;
    }

    public static User createDefaultUser(Tenant tenant) {
        return createUser(1L, "testuser", null, UserRole.SUPERADMIN, tenant, new HashSet<>());
    }

    public static UserDto.UserResponse createUserResponse(Long id, String username, UserRole role, Tenant tenant, boolean isEditable) {
        return UserDto.UserResponse.builder()
                .id(id)
                .username(username)
                .role(role)
                .tenant(tenant)
                .isEditable(isEditable)
                .build();
    }

    public static UserDto.UserResponse createDefaultUserResponse(Tenant tenant) {
        return createUserResponse(1L, "testuser", UserRole.ADMIN, tenant, true);
    }

    public static void setUserContextHolder(User user) {
        UserContextHolder.set(new UserContext(user));
    }
}
