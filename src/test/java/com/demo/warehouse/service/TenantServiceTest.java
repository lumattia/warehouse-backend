package com.demo.warehouse.service;

import com.demo.warehouse.domain.ModuleType;
import com.demo.warehouse.domain.Tenant;
import com.demo.warehouse.domain.User;
import com.demo.warehouse.domain.UserRole;
import com.demo.warehouse.mapper.IdName;
import com.demo.warehouse.mapper.IdNameImpl;
import com.demo.warehouse.mapper.TenantDtos;
import com.demo.warehouse.mapper.TenantMapper;
import com.demo.warehouse.repository.TenantRepository;
import com.demo.warehouse.repository.UserRepository;
import com.demo.warehouse.tenantFilter.UserContextHolder;
import com.demo.warehouse.testutils.TestFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantMapper tenantMapper;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TenantService tenantService;

    private Tenant tenant;
    private User user;
    private TenantDtos.TenantResponse tenantResponse;

    @BeforeEach
    void setUp() {
        tenant = TestFactory.createDefaultTenant();
        user = TestFactory.createDefaultUser(tenant);
        TestFactory.setUserContextHolder(user);
        tenantResponse = TestFactory.createDefaultTenantResponse(tenant.getId());
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void page_ShouldReturnAllTenantsForSuperAdmin() {
        Page<Tenant> page = new PageImpl<>(List.of(tenant), PageRequest.of(0, 10), 1);
        when(tenantRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(tenantMapper.toResponse(any(Tenant.class))).thenReturn(tenantResponse);

        Page<TenantDtos.TenantResponse> result = tenantService.page(PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(tenantRepository, times(1)).findAll(any(Pageable.class));
        verify(tenantMapper, times(1)).toResponse(any(Tenant.class));
    }

    @Test
    void page_ShouldReturnAllowedTenantsForReseller() {
        user.setRole(UserRole.RESELLER);
        user.setAllowedTenants(Set.of(tenant));
        TestFactory.setUserContextHolder(user);

        Page<Tenant> page = new PageImpl<>(List.of(tenant), PageRequest.of(0, 10), 1);
        when(tenantRepository.findAll(
            ArgumentMatchers.<Specification<Tenant>>any(), 
            any(Pageable.class)
        )).thenReturn(page);
        when(tenantMapper.toResponse(any(Tenant.class))).thenReturn(tenantResponse);

        Page<TenantDtos.TenantResponse> result = tenantService.page(PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(tenantRepository, times(1)).findAll(ArgumentMatchers.<Specification<Tenant>>any(), any(Pageable.class));
        verify(tenantMapper, times(1)).toResponse(any(Tenant.class));
    }

    @Test
    void page_ShouldReturnOwnTenantForAdmin() {
        user.setRole(UserRole.ADMIN);
        TestFactory.setUserContextHolder(user);

        Page<Tenant> page = new PageImpl<>(List.of(tenant), PageRequest.of(0, 10), 1);
        when(tenantRepository.findAll(ArgumentMatchers.<Specification<Tenant>>any(), any(Pageable.class))).thenReturn(page);
        when(tenantMapper.toResponse(any(Tenant.class))).thenReturn(tenantResponse);

        Page<TenantDtos.TenantResponse> result = tenantService.page(PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(tenantRepository, times(1)).findAll(ArgumentMatchers.<Specification<Tenant>>any(), any(Pageable.class));
        verify(tenantMapper, times(1)).toResponse(any(Tenant.class));
    }

    @Test
    void list_ShouldReturnAllTenantsForSuperAdmin() {
        user.setRole(UserRole.SUPERADMIN);
        TestFactory.setUserContextHolder(user);

        IdName<UUID> idName = new IdNameImpl<>(tenant.getId(), tenant.getName());
        List<IdName<UUID>> list = List.of(idName);
        when(tenantRepository.listByIdName()).thenReturn(list);

        List<IdName<UUID>> result = tenantService.list();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(tenantRepository, times(1)).listByIdName();
    }

    @Test
    void list_ShouldReturnAllowedTenantsForReseller() {
        user.setRole(UserRole.RESELLER);
        user.setAllowedTenants(Set.of(tenant));
        TestFactory.setUserContextHolder(user);

        List<IdName<UUID>> result = tenantService.list();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(tenantRepository, never()).listByIdName();
    }

    @Test
    void detail_ShouldReturnTenantForSuperAdmin() {
        user.setRole(UserRole.SUPERADMIN);
        TestFactory.setUserContextHolder(user);

        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(tenantMapper.toResponse(tenant)).thenReturn(tenantResponse);

        TenantDtos.TenantResponse result = tenantService.detail(tenant.getId());

        assertNotNull(result);
        verify(tenantRepository, times(1)).findById(tenant.getId());
        verify(tenantMapper, times(1)).toResponse(tenant);
    }

    @Test
    void detail_ShouldReturnAllowedTenantForReseller() {
        user.setRole(UserRole.RESELLER);
        user.setAllowedTenants(Set.of(tenant));
        TestFactory.setUserContextHolder(user);

        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(tenantMapper.toResponse(tenant)).thenReturn(tenantResponse);

        TenantDtos.TenantResponse result = tenantService.detail(tenant.getId());

        assertNotNull(result);
        verify(tenantRepository, times(1)).findById(tenant.getId());
        verify(tenantMapper, times(1)).toResponse(tenant);
    }

    @Test
    void detail_ShouldThrowWhenTenantNotAllowedForReseller() {
        user.setRole(UserRole.RESELLER);
        user.setAllowedTenants(Set.of());
        TestFactory.setUserContextHolder(user);

        assertThrows(RuntimeException.class, () -> tenantService.detail(tenant.getId()));
        verify(tenantRepository, never()).findById(any(UUID.class));
    }

    @Test
    void detail_ShouldReturnOwnTenantForAdmin() {
        user.setRole(UserRole.ADMIN);
        TestFactory.setUserContextHolder(user);

        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(tenantMapper.toResponse(tenant)).thenReturn(tenantResponse);

        TenantDtos.TenantResponse result = tenantService.detail(tenant.getId());

        assertNotNull(result);
        verify(tenantRepository, times(1)).findById(tenant.getId());
        verify(tenantMapper, times(1)).toResponse(tenant);
    }

    @Test
    void detail_ShouldThrowWhenAdminViewsOtherTenant() {
        user.setRole(UserRole.ADMIN);
        UUID otherTenantId = UUID.randomUUID();
        TestFactory.setUserContextHolder(user);

        assertThrows(RuntimeException.class, () -> tenantService.detail(otherTenantId));
        verify(tenantRepository, never()).findById(any(UUID.class));
    }

    @Test
    void detail_ShouldThrowWhenTenantNotFound() {
        user.setRole(UserRole.SUPERADMIN);
        TestFactory.setUserContextHolder(user);

        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> tenantService.detail(tenant.getId()));
        verify(tenantRepository, times(1)).findById(tenant.getId());
    }

    @Test
    void create_ShouldCreateTenantForSuperAdmin() {
        user.setRole(UserRole.SUPERADMIN);
        TestFactory.setUserContextHolder(user);

        TenantDtos.TenantCreateRequest request = new TenantDtos.TenantCreateRequest("New Tenant", Set.of(ModuleType.DRESS));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        when(tenantMapper.toResponse(tenant)).thenReturn(tenantResponse);

        TenantDtos.TenantResponse result = tenantService.create(request);

        assertNotNull(result);
        verify(tenantRepository, times(1)).save(any(Tenant.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void create_ShouldCreateTenantAndAddToReseller() {
        user.setRole(UserRole.RESELLER);
        TestFactory.setUserContextHolder(user);

        TenantDtos.TenantCreateRequest request = new TenantDtos.TenantCreateRequest("New Tenant", Set.of(ModuleType.DRESS));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tenantMapper.toResponse(tenant)).thenReturn(tenantResponse);

        TenantDtos.TenantResponse result = tenantService.create(request);

        assertNotNull(result);
        verify(tenantRepository, times(1)).save(any(Tenant.class));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void update_ShouldUpdateTenant() {
        TenantDtos.TenantUpdateRequest request = new TenantDtos.TenantUpdateRequest(tenant.getId(), "Updated Tenant", Set.of(ModuleType.DRESS, ModuleType.DRESS_MOVEMENT));
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        when(tenantMapper.toResponse(tenant)).thenReturn(tenantResponse);

        TenantDtos.TenantResponse result = tenantService.update(request);

        assertNotNull(result);
        assertEquals("Updated Tenant", tenant.getName());
        verify(tenantRepository, times(1)).findById(tenant.getId());
        verify(tenantRepository, times(1)).save(any(Tenant.class));
    }

    @Test
    void update_ShouldThrowWhenTenantNotFound() {
        TenantDtos.TenantUpdateRequest request = new TenantDtos.TenantUpdateRequest(tenant.getId(), "Updated Tenant", Set.of(ModuleType.DRESS));
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> tenantService.update(request));
        verify(tenantRepository, times(1)).findById(tenant.getId());
        verify(tenantRepository, never()).save(any(Tenant.class));
    }

    @Test
    void delete_ShouldDeleteTenant() {
        doNothing().when(tenantRepository).deleteById(tenant.getId());

        tenantService.delete(tenant.getId());

        verify(tenantRepository, times(1)).deleteById(tenant.getId());
    }
}
