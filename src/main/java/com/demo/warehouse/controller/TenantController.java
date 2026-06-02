package com.demo.warehouse.controller;

import com.demo.warehouse.mapper.IdName;
import com.demo.warehouse.mapper.TenantDtos;
import com.demo.warehouse.mapper.UserDto;
import com.demo.warehouse.service.TenantService;
import com.demo.warehouse.service.UserService;
import com.demo.warehouse.specification.TenantSpecification;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;
    private final UserService userService;

    @GetMapping("/page")
    @PreAuthorize("@securityService.canManageTenants()")
    public Page<TenantDtos.TenantResponse> page(TenantDtos.TenantFilterRequest filter, Pageable pageable) {
        return tenantService.page(TenantSpecification.filterBy(filter), pageable);
    }

    @GetMapping("/list")
    @PreAuthorize("@securityService.canManageTenants()")
    public List<IdName<UUID>> list() {
        return tenantService.list();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityService.canManageTenants()")
    public TenantDtos.TenantResponse detail(@PathVariable UUID id) {
        return tenantService.detail(id);
    }

    @PostMapping("/create")
    @PreAuthorize("@securityService.canManageTenants()")
    public TenantDtos.TenantResponse create(@RequestBody TenantDtos.TenantCreateRequest request) {
        return tenantService.create(request);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("@securityService.canManageTenants()")
    public TenantDtos.TenantResponse update(@PathVariable UUID id, @RequestBody TenantDtos.TenantUpdateRequest request) {
        if (!id.equals(request.id())) {
            throw new IllegalArgumentException("El ID de la URL no coincide con el ID del cuerpo");
        }
        return tenantService.update(request);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("@securityService.canManageTenants()")
    public void delete(@PathVariable UUID id) {
        tenantService.delete(id);
    }

    @PostMapping("/switch/{tenantId}")
    public UserDto.LoggedUserDto switchTenant(@PathVariable UUID tenantId) {
        return userService.switchTenant(tenantId);
    }
}
