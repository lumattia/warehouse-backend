package com.demo.warehouse.scheduler;

import com.demo.warehouse.domain.Tenant;
import com.demo.warehouse.domain.User;
import com.demo.warehouse.domain.UserRole;
import com.demo.warehouse.repository.TenantRepository;
import com.demo.warehouse.repository.UserRepository;
import com.demo.warehouse.service.Auth0ManagementService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantCleanupSchedulerTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Auth0ManagementService auth0ManagementService;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private TenantCleanupScheduler tenantCleanupScheduler;

    private Tenant expiredTenant;
    private User userWithAuth0Sub;
    private User userWithoutAuth0Sub;

    @BeforeEach
    void setUp() {
        expiredTenant = new Tenant();
        expiredTenant.setId(UUID.randomUUID());
        expiredTenant.setName("Expired Tenant");
        expiredTenant.setExpiresAt(Instant.now().minusSeconds(3600));

        userWithAuth0Sub = new User(1L, "user1", null, "auth0|123456", UserRole.USER, new HashSet<>());
        userWithAuth0Sub.setTenant(expiredTenant);

        userWithoutAuth0Sub = new User(2L, "user2", null, null, UserRole.USER, new HashSet<>());
        userWithoutAuth0Sub.setTenant(expiredTenant);
    }

    @Test
    void cleanupExpiredTenants_ShouldDoNothingWhenNoExpiredTenants() {
        when(tenantRepository.findByExpiresAtBefore(any(Instant.class))).thenReturn(Collections.emptyList());

        tenantCleanupScheduler.cleanupExpiredTenants();

        verify(tenantRepository, times(1)).findByExpiresAtBefore(any(Instant.class));
        verify(userRepository, never()).findByTenantId(any(UUID.class));
        verify(auth0ManagementService, never()).deleteUser(anyString());
        verify(tenantRepository, never()).deleteAll(anyList());
    }

    @Test
    void cleanupExpiredTenants_ShouldDeleteExpiredTenantsAndAuth0Users() {
        List<Tenant> expiredTenants = List.of(expiredTenant);
        List<User> users = List.of(userWithAuth0Sub, userWithoutAuth0Sub);

        when(tenantRepository.findByExpiresAtBefore(any(Instant.class))).thenReturn(expiredTenants);
        when(userRepository.findByTenantId(expiredTenant.getId())).thenReturn(users);
        doNothing().when(tenantRepository).deleteAll(expiredTenants);

        tenantCleanupScheduler.cleanupExpiredTenants();

        verify(tenantRepository, times(1)).findByExpiresAtBefore(any(Instant.class));
        verify(userRepository, times(1)).findByTenantId(expiredTenant.getId());
        verify(auth0ManagementService, times(1)).deleteUser("auth0|123456");
        verifyNoMoreInteractions(auth0ManagementService);
        verify(tenantRepository, times(1)).deleteAll(expiredTenants);
    }

    @Test
    void cleanupExpiredTenants_ShouldSkipUsersWithoutAuth0Sub() {
        List<Tenant> expiredTenants = List.of(expiredTenant);
        List<User> users = List.of(userWithoutAuth0Sub);

        when(tenantRepository.findByExpiresAtBefore(any(Instant.class))).thenReturn(expiredTenants);
        when(userRepository.findByTenantId(expiredTenant.getId())).thenReturn(users);
        doNothing().when(tenantRepository).deleteAll(expiredTenants);

        tenantCleanupScheduler.cleanupExpiredTenants();

        verify(tenantRepository, times(1)).findByExpiresAtBefore(any(Instant.class));
        verify(userRepository, times(1)).findByTenantId(expiredTenant.getId());
        verify(auth0ManagementService, never()).deleteUser(anyString());
        verify(tenantRepository, times(1)).deleteAll(expiredTenants);
    }

    @Test
    void cleanupExpiredTenants_ShouldSkipUsersWithNonAuth0Sub() {
        userWithAuth0Sub.setAuth0Sub("github|123456");
        List<Tenant> expiredTenants = List.of(expiredTenant);
        List<User> users = List.of(userWithAuth0Sub);

        when(tenantRepository.findByExpiresAtBefore(any(Instant.class))).thenReturn(expiredTenants);
        when(userRepository.findByTenantId(expiredTenant.getId())).thenReturn(users);
        doNothing().when(tenantRepository).deleteAll(expiredTenants);

        tenantCleanupScheduler.cleanupExpiredTenants();

        verify(tenantRepository, times(1)).findByExpiresAtBefore(any(Instant.class));
        verify(userRepository, times(1)).findByTenantId(expiredTenant.getId());
        verify(auth0ManagementService, never()).deleteUser(anyString());
        verify(tenantRepository, times(1)).deleteAll(expiredTenants);
    }

    @Test
    void cleanupExpiredTenants_ShouldHandleMultipleExpiredTenants() {
        Tenant expiredTenant2 = new Tenant();
        expiredTenant2.setId(UUID.randomUUID());
        expiredTenant2.setName("Expired Tenant 2");
        expiredTenant2.setExpiresAt(Instant.now().minusSeconds(3600));

        User user2 = new User(3L, "user3", null, "auth0|789012", UserRole.USER, new HashSet<>());
        user2.setTenant(expiredTenant2);

        List<Tenant> expiredTenants = List.of(expiredTenant, expiredTenant2);

        when(tenantRepository.findByExpiresAtBefore(any(Instant.class))).thenReturn(expiredTenants);
        when(userRepository.findByTenantId(expiredTenant.getId())).thenReturn(List.of(userWithAuth0Sub));
        when(userRepository.findByTenantId(expiredTenant2.getId())).thenReturn(List.of(user2));
        doNothing().when(tenantRepository).deleteAll(expiredTenants);

        tenantCleanupScheduler.cleanupExpiredTenants();

        verify(tenantRepository, times(1)).findByExpiresAtBefore(any(Instant.class));
        verify(userRepository, times(1)).findByTenantId(expiredTenant.getId());
        verify(userRepository, times(1)).findByTenantId(expiredTenant2.getId());
        verify(auth0ManagementService, times(1)).deleteUser("auth0|123456");
        verify(auth0ManagementService, times(1)).deleteUser("auth0|789012");
        verify(tenantRepository, times(1)).deleteAll(expiredTenants);
    }
}
