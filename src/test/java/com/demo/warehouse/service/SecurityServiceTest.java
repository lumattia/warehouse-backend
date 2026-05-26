package com.demo.warehouse.service;

import com.demo.warehouse.domain.ModuleType;
import com.demo.warehouse.domain.Tenant;
import com.demo.warehouse.domain.User;
import com.demo.warehouse.domain.UserRole;
import com.demo.warehouse.tenantFilter.UserContextHolder;
import com.demo.warehouse.testutils.TestFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SecurityServiceTest {

    private SecurityService securityService;
    private User testUser;
    private Tenant testTenant;

    @BeforeEach
    void setUp() {
        securityService = new SecurityService();
        
        testTenant = new Tenant();
        testTenant.setId(UUID.randomUUID());
        testTenant.setName("Test Tenant");
        testTenant.setModules(Set.of(ModuleType.DRESS, ModuleType.DRESS_MOVEMENT));


        testUser = new User(1L, "testuser", null, "", UserRole.SUPERADMIN, Set.of(testTenant));
        testUser.setTenant(testTenant);
        TestFactory.setUserContextHolder(testUser);
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void hasAnyRole_shouldReturnTrueWhenUserHasRole() {
        testUser.setRole(UserRole.ADMIN);
        assertTrue(securityService.hasAnyRole("ADMIN"));
        assertTrue(securityService.hasAnyRole("ADMIN", "USER"));
        assertFalse(securityService.hasAnyRole("SUPERADMIN"));
        assertFalse(securityService.hasAnyRole("USER"));
    }

    @Test
    void hasAnyRole_shouldReturnFalseWhenUserDoesNotHaveRole() {
        testUser.setRole(UserRole.USER);
        assertFalse(securityService.hasAnyRole("ADMIN"));
        assertFalse(securityService.hasAnyRole("SUPERADMIN"));
        assertTrue(securityService.hasAnyRole("USER"));
    }

    @Test
    void isAtLeast_shouldReturnTrueWhenUserHasHigherRole() {
        testUser.setRole(UserRole.SUPERADMIN);
        assertTrue(securityService.isAtLeast("ADMIN"));
        assertTrue(securityService.isAtLeast("RESELLER"));
        assertTrue(securityService.isAtLeast("USER"));
        assertTrue(securityService.isAtLeast("SUPERADMIN"));
    }

    @Test
    void isAtLeast_shouldReturnTrueWhenUserHasSameRole() {
        testUser.setRole(UserRole.ADMIN);
        assertTrue(securityService.isAtLeast("ADMIN"));
        assertFalse(securityService.isAtLeast("RESELLER"));
        assertFalse(securityService.isAtLeast("SUPERADMIN"));
        assertTrue(securityService.isAtLeast("USER"));
    }

    @Test
    void isAtLeast_shouldReturnFalseWhenUserHasLowerRole() {
        testUser.setRole(UserRole.USER);
        assertFalse(securityService.isAtLeast("ADMIN"));
        assertFalse(securityService.isAtLeast("RESELLER"));
        assertFalse(securityService.isAtLeast("SUPERADMIN"));
        assertTrue(securityService.isAtLeast("USER"));
    }

    @Test
    void hasModule_shouldReturnTrueWhenTenantHasModule() {
        testTenant.setModules(Set.of(ModuleType.DRESS, ModuleType.DRESS_MOVEMENT));
        assertTrue(securityService.hasModule("DRESS"));
        assertTrue(securityService.hasModule("DRESS_MOVEMENT"));
        assertFalse(securityService.hasModule("USER"));
    }

    @Test
    void hasModule_shouldReturnFalseWhenTenantDoesNotHaveModule() {
        testTenant.setModules(Set.of(ModuleType.DRESS));
        assertTrue(securityService.hasModule("DRESS"));
        assertFalse(securityService.hasModule("DRESS_MOVEMENT"));
        assertFalse(securityService.hasModule("USER"));
    }

    @Test
    void canManageTenants_shouldReturnTrueForSuperAdmin() {
        testUser.setRole(UserRole.SUPERADMIN);
        testUser.setAllowedTenants(Set.of());
        assertTrue(securityService.canManageTenants());
    }

    @Test
    void canManageTenants_shouldReturnTrueForResellerWithAllowedTenants() {
        testUser.setRole(UserRole.RESELLER);
        testUser.setAllowedTenants(Set.of(testTenant));
        assertTrue(securityService.canManageTenants());
    }

    @Test
    void canManageTenants_shouldReturnFalseForResellerWithoutAllowedTenants() {
        testUser.setRole(UserRole.RESELLER);
        testUser.setAllowedTenants(Set.of());
        assertFalse(securityService.canManageTenants());
    }

    @Test
    void canManageTenants_shouldReturnFalseForAdmin() {
        testUser.setRole(UserRole.ADMIN);
        assertFalse(securityService.canManageTenants());
    }

    @Test
    void canManageTenants_shouldReturnFalseForUser() {
        testUser.setRole(UserRole.USER);
        assertFalse(securityService.canManageTenants());
    }
}
