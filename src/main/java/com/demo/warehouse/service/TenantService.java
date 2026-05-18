package com.demo.warehouse.service;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.demo.warehouse.domain.Tenant;
import com.demo.warehouse.mapper.IdName;
import com.demo.warehouse.mapper.IdNameImpl;
import com.demo.warehouse.mapper.TenantDtos;
import com.demo.warehouse.mapper.TenantMapper;
import com.demo.warehouse.repository.TenantRepository;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantMapper tenantMapper;
    private final TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    public Page<TenantDtos.TenantResponse> page(@NonNull Pageable pageable) {
        return tenantRepository.findAll(pageable).map(tenantMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public List<IdName<UUID>> list() {
        return tenantRepository.findAll().stream()
            .map(tenant -> {
                IdNameImpl<UUID> idName = new IdNameImpl<>();
                idName.setId(tenant.getId());
                idName.setName(tenant.getName());
                return (IdName<UUID>) idName;
            })
            .toList();
    }
    
    @Transactional(readOnly = true)
    public TenantDtos.TenantResponse detail(UUID id) {
        var tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        return tenantMapper.toResponse(tenant);
    }

    @Transactional
    public TenantDtos.TenantResponse create(TenantDtos.TenantCreateRequest request) {
        var tenant = new Tenant();
        tenant.setName(request.name());
        tenant.setModules(request.modules());
        return tenantMapper.toResponse(tenantRepository.save(tenant));
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
