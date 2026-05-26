package com.demo.warehouse.service;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

import jakarta.annotation.Nonnull;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantMapper tenantMapper;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<TenantDtos.TenantResponse> page(@Nonnull Pageable pageable) {
        User currentUser = UserContextHolder.get().getUser();

        if (currentUser.getRole() == UserRole.RESELLER) {
            // RESELLER solo ve sus allowedTenants
            List<UUID> allowedTenantIds = currentUser.getAllowedTenants().stream()
                .map(Tenant::getId)
                .toList();
            Specification<Tenant> spec = (root, query, cb) ->
                root.get("id").in(allowedTenantIds);
            return tenantRepository.findAll(spec, pageable).map(tenantMapper::toResponse);
        }

        if (currentUser.getRole() == UserRole.ADMIN) {
            // ADMIN solo ve su propio tenant
            Specification<Tenant> spec = (root, query, cb) ->
                cb.equal(root.get("id"), currentUser.getTenant().getId());
            return tenantRepository.findAll(spec, pageable).map(tenantMapper::toResponse);
        }

        // SUPERADMIN ve todos
        return tenantRepository.findAll(pageable).map(tenantMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<IdName<UUID>> list() {
        User currentUser = UserContextHolder.get().getUser();

        if (currentUser.getRole() == UserRole.RESELLER) {
            // RESELLER solo ve sus allowedTenants
            return currentUser.getAllowedTenants().stream()
                .map(tenant -> {
                    IdNameImpl<UUID> idName = new IdNameImpl<>(tenant.getId(), tenant.getName());
                    return (IdName<UUID>) idName;
                })
                .sorted((a, b) -> a.getName().compareTo(b.getName()))
                .toList();
        }

        // SUPERADMIN ve todos
        return tenantRepository.listByIdName();
    }

    @Transactional(readOnly = true)
    public TenantDtos.TenantResponse detail(UUID id) {
        User currentUser = UserContextHolder.get().getUser();

        if (currentUser.getRole() == UserRole.RESELLER) {
            // RESELLER solo puede ver sus allowedTenants
            if (!currentUser.getAllowedTenants().stream().anyMatch(t -> t.getId().equals(id))) {
                throw new RuntimeException("Tenant not allowed for this RESELLER");
            }
        }

        if (currentUser.getRole() == UserRole.ADMIN) {
            // ADMIN solo puede ver su propio tenant
            if (!currentUser.getTenant().getId().equals(id)) {
                throw new RuntimeException("ADMIN can only view their own tenant");
            }
        }

        var tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        return tenantMapper.toResponse(tenant);
    }

    @Transactional
    public TenantDtos.TenantResponse create(TenantDtos.TenantCreateRequest request) {
        var tenant = new Tenant();
        tenant.setName(request.name());
        tenant.setModules(request.modules());
        tenant = tenantRepository.save(tenant);

        // If current user is RESELLER, add the tenant to their allowedTenants
        User currentUser = UserContextHolder.get().getUser();
        if (currentUser.getRole() == UserRole.RESELLER) {
            currentUser.getAllowedTenants().add(tenant);
            userRepository.save(currentUser);
        }

        return tenantMapper.toResponse(tenant);
    }

    @Transactional
    public TenantDtos.TenantResponse update(TenantDtos.TenantUpdateRequest request) {
        var tenant = tenantRepository.findById(request.id())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        tenant.setName(request.name());
        tenant.setModules(request.modules());
        tenantRepository.save(tenant);
        return tenantMapper.toResponse(tenant);
    }
    
    @Transactional
    public void delete(UUID toDeleteId) {
        tenantRepository.deleteById(toDeleteId);
    }
}
