package com.demo.warehouse.service;

import com.demo.warehouse.domain.Tenant;
import com.demo.warehouse.domain.User;
import com.demo.warehouse.domain.UserRole;
import com.demo.warehouse.mapper.IdName;
import com.demo.warehouse.mapper.UserDto;
import com.demo.warehouse.mapper.UserMapper;
import com.demo.warehouse.repository.TenantRepository;
import com.demo.warehouse.repository.UserRepository;
import com.demo.warehouse.tenantFilter.UserContext;
import com.demo.warehouse.tenantFilter.UserContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private Tenant tenant;
    private UserDto.UserResponse userResponse;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(UUID.randomUUID());
        tenant.setName("Test Tenant");

        user = new User(1L, "testuser", null, "", UserRole.SUPERADMIN, Set.of(tenant));
        user.setTenant(tenant);
        UserContext mockContext = UserContext.builder().realUser(user).build();


        UserContextHolder.set(mockContext);
        userResponse = UserDto.UserResponse.builder()
                .id(1L)
                .username("testuser")
                .role(UserRole.ADMIN)
                .tenant(tenant)
                .isEditable(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void page_ShouldReturnPageOfUsers() {
        Page<User> page = new PageImpl<>(Collections.singletonList(user), PageRequest.of(0, 10), 1);
        when(userRepository.getBySpec(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);
        Specification<User> dummySpec = (root, query, criteriaBuilder) -> null;
        Page<UserDto.UserResponse> result = userService.page(dummySpec, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository, times(1)).getBySpec(any(Specification.class), any(Pageable.class));
        verify(userMapper, times(1)).toResponse(any(User.class));
    }

    @Test
    void list_ShouldReturnListOfUsers() {
        List<IdName<Long>> list = Collections.singletonList(new IdNameImpl(1L, "testuser"));
        when(userRepository.getAllAsIdName()).thenReturn(list);

        List<IdName<Long>> result = userService.list();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository, times(1)).getAllAsIdName();
    }

    @Test
    void detail_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserDto.UserResponse result = userService.detail(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(userRepository, times(1)).findById(1L);
        verify(userMapper, times(1)).toResponse(user);
    }

    @Test
    void detail_ShouldThrowWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.detail(1L));
        verify(userRepository, times(1)).findById(1L);
        verify(userMapper, never()).toResponse(any(User.class));
    }

    @Test
    void create_ShouldCreateUserWithoutAllowedTenants() {
        UserDto.UserCreateRequest request = new UserDto.UserCreateRequest("newuser", UserRole.USER, null);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserDto.UserResponse result = userService.create(request);

        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toResponse(user);
    }

    @Test
    void create_ShouldCreateUserWithAllowedTenants() {
        Set<UUID> tenantIds = Set.of(tenant.getId());
        UserDto.UserCreateRequest request = new UserDto.UserCreateRequest("newuser", UserRole.RESELLER, tenantIds);
        when(tenantRepository.findAllById(tenantIds)).thenReturn(List.of(tenant));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserDto.UserResponse result = userService.create(request);

        assertNotNull(result);
        verify(tenantRepository, times(1)).findAllById(tenantIds);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toResponse(user);
    }

    @Test
    void update_ShouldUpdateUser() {
        UserDto.UserUpdateRequest request = new UserDto.UserUpdateRequest(1L, "updateduser", UserRole.ADMIN, null);
        when(userRepository.getByIdOrThrow(1L)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserDto.UserResponse result = userService.update(request);

        assertNotNull(result);
        assertEquals("updateduser", user.getUsername());
        verify(userRepository, times(1)).getByIdOrThrow(1L);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toResponse(user);
    }

    @Test
    void update_ShouldUpdateUserWithAllowedTenants() {
        Set<UUID> tenantIds = Set.of(tenant.getId());
        UserDto.UserUpdateRequest request = new UserDto.UserUpdateRequest(1L, "updateduser", UserRole.RESELLER, tenantIds);
        when(userRepository.getByIdOrThrow(1L)).thenReturn(user);
        when(tenantRepository.findAllById(tenantIds)).thenReturn(List.of(tenant));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserDto.UserResponse result = userService.update(request);

        assertNotNull(result);
        verify(tenantRepository, times(1)).findAllById(tenantIds);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toResponse(user);
    }

    @Test
    void delete_ShouldDeleteUser() {
        when(userRepository.getByIdOrThrow(1L)).thenReturn(user);
        doNothing().when(userRepository).delete(any(User.class));

        userService.delete(1L);

        verify(userRepository, times(1)).getByIdOrThrow(1L);
        verify(userRepository, times(1)).delete(any(User.class));
    }

    @Test
    void switchTenant_ShouldSwitchTenantForSuperAdmin() {
        UserContext context = UserContext.builder().realUser(user).build();
        UserContextHolder.set(context);

        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toLogged(user)).thenReturn(new UserDto.LoggedUserDto(1L, "testuser", "auth0sub", UserRole.ADMIN, tenant));

        UserDto.LoggedUserDto result = userService.switchTenant(tenant.getId());

        assertNotNull(result);
        verify(tenantRepository, times(1)).findById(tenant.getId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void switchTenant_ShouldSwitchTenantForReseller() {
        user.setRole(UserRole.RESELLER);
        user.setAllowedTenants(Set.of(tenant));
        UserContext context = UserContext.builder().realUser(user).build();
        UserContextHolder.set(context);

        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toLogged(user)).thenReturn(new UserDto.LoggedUserDto(1L, "testuser", "auth0sub", UserRole.RESELLER, tenant));

        UserDto.LoggedUserDto result = userService.switchTenant(tenant.getId());

        assertNotNull(result);
        verify(tenantRepository, times(1)).findById(tenant.getId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void switchTenant_ShouldThrowWhenTenantNotAllowedForReseller() {
        user.setRole(UserRole.RESELLER);
        user.setAllowedTenants(Set.of());
        UserContext context = UserContext.builder().realUser(user).build();
        UserContextHolder.set(context);

        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));

        assertThrows(RuntimeException.class, () -> userService.switchTenant(tenant.getId()));
        verify(tenantRepository, times(1)).findById(tenant.getId());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void switchTenant_ShouldThrowWhenTenantNotFound() {
        UserContext context = UserContext.builder().realUser(user).build();
        UserContextHolder.set(context);

        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.switchTenant(tenant.getId()));
        verify(tenantRepository, times(1)).findById(tenant.getId());
    }

    @Test
    void switchTenant_ShouldThrowForUserRole() {
        user.setRole(UserRole.USER);
        UserContext context = UserContext.builder().realUser(user).build();
        UserContextHolder.set(context);

        assertThrows(RuntimeException.class, () -> userService.switchTenant(tenant.getId()));
        verify(tenantRepository, never()).findById(any(UUID.class));
    }
}
