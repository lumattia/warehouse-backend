package com.demo.warehouse.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.demo.warehouse.domain.User;
import com.demo.warehouse.domain.UserRole;
import com.demo.warehouse.mapper.IdName;
import com.demo.warehouse.mapper.UserDto;
import com.demo.warehouse.mapper.UserMapper;
import com.demo.warehouse.repository.TenantRepository;
import com.demo.warehouse.repository.UserRepository;
import com.demo.warehouse.tenantFilter.UserContextHolder;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    public Page<UserDto.UserResponse> page(Specification<User> spec, @NonNull Pageable pageable) {
        return userRepository.getBySpec(spec, pageable).map(userMapper::toResponseWithTenants);
    }
    
    @Transactional(readOnly = true)
    public List<IdName<Long>> list() {
        return userRepository.getAllAsIdName();
    }
    
    @Transactional(readOnly = true)
    public UserDto.UserResponse detail(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toResponseWithTenants(user);
    }

    @Transactional
    public UserDto.UserResponse create(UserDto.UserCreateRequest request) {
        User currentUser = UserContextHolder.get().getUser();
        validateRoleAssignment(currentUser.getRole(), request.role());

        var user = new User();
        user.setUsername(request.username());
        user.setRole(request.role());

        if (request.allowedTenantIds() != null && !request.allowedTenantIds().isEmpty()) {
            Set<com.demo.warehouse.domain.Tenant> tenants = tenantRepository.findAllById(request.allowedTenantIds())
                .stream()
                .collect(java.util.stream.Collectors.toSet());
            user.setAllowedTenants(tenants);
        }

        return userMapper.toResponseWithTenants(userRepository.save(user));
    }

    @Transactional
    public UserDto.UserResponse update(UserDto.UserUpdateRequest request) {
        User currentUser = UserContextHolder.get().getUser();
        validateRoleAssignment(currentUser.getRole(), request.role());

        var user = userRepository.getByIdOrThrow(request.id());
        user.setUsername(request.username());
        user.setRole(request.role());

        if (request.allowedTenantIds() != null) {
            Set<com.demo.warehouse.domain.Tenant> tenants = tenantRepository.findAllById(request.allowedTenantIds())
                .stream()
                .collect(java.util.stream.Collectors.toSet());
            user.setAllowedTenants(tenants);
        }

        userRepository.save(user);
        return userMapper.toResponseWithTenants(user);
    }
    
    @Transactional
    public void delete(Long toDeleteId) {
        userRepository.deleteById(toDeleteId);
    }

    @Transactional
    public UserDto.LoggedUserDto switchTenant(UUID tenantId) {
        User currentUser = UserContextHolder.get().getUser();

        com.demo.warehouse.domain.Tenant targetTenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        // SUPERADMIN can change to any tenant
        if (currentUser.getRole() == UserRole.SUPERADMIN) {
            currentUser.setTenant(targetTenant);
            userRepository.save(currentUser);
            return userMapper.toLogged(currentUser);
        }

        // RESELLER can only change to tenants in their allowedTenants
        if (currentUser.getRole() == UserRole.RESELLER) {
            if (!currentUser.getAllowedTenants().contains(targetTenant)) {
                throw new RuntimeException("Tenant not allowed for this RESELLER");
            }
            currentUser.setTenant(targetTenant);
            userRepository.save(currentUser);
            return userMapper.toLogged(currentUser);
        }

        throw new RuntimeException("Only SUPERADMIN and RESELLER can switch tenants");
    }

    private void validateRoleAssignment(UserRole currentRole, UserRole targetRole) {
        switch (currentRole) {
            case SUPERADMIN:
                // SUPERADMIN can assign any role
                break;
            case RESELLER:
                // RESELLER can only assign ADMIN or USER
                if (targetRole == UserRole.RESELLER || targetRole == UserRole.SUPERADMIN) {
                    throw new RuntimeException("RESELLER can only assign ADMIN or USER roles");
                }
                break;
            case ADMIN:
                // ADMIN solo puede asignar ADMIN o USER
                if (targetRole == UserRole.RESELLER || targetRole == UserRole.SUPERADMIN) {
                    throw new RuntimeException("ADMIN can only assign ADMIN or USER roles");
                }
                break;
            case USER:
                throw new RuntimeException("USER cannot assign roles");
        }
    }
}
