package com.demo.warehouse.controller;

import com.demo.warehouse.domain.User;
import com.demo.warehouse.domain.UserRole;
import com.demo.warehouse.mapper.IdName;
import com.demo.warehouse.mapper.TenantDtos;
import com.demo.warehouse.service.TenantService;
import com.demo.warehouse.tenantFilter.UserContextHolder;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @GetMapping("/page")
    public Page<TenantDtos.TenantResponse> page(Pageable pageable) {
        return tenantService.page(pageable);
    }

    @GetMapping("/list")
    public List<IdName<UUID>> list() {
        return tenantService.list();
    }

    @GetMapping("/{id}")
    public TenantDtos.TenantResponse detail(@PathVariable UUID id) {
        return tenantService.detail(id);
    }

    @PostMapping("/create")
    public TenantDtos.TenantResponse create(@RequestBody TenantDtos.TenantCreateRequest request) {
        return tenantService.create(request);
    }

    @PutMapping("/update/{id}")
    public TenantDtos.TenantResponse update(@PathVariable UUID id, @RequestBody TenantDtos.TenantUpdateRequest request) {
        return tenantService.update(request);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable UUID id) {
        tenantService.delete(id);
    }
}
