package com.demo.warehouse.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.warehouse.mapper.IdName;
import com.demo.warehouse.mapper.DressDtos;
import com.demo.warehouse.service.DressService;
import com.demo.warehouse.specification.DressSpecification;

@RestController
@RequestMapping("/dresses")
@RequiredArgsConstructor
@PreAuthorize("@securityService.hasModule('DRESS')")
public class DressController {
    private final DressService dressService;

    @GetMapping("/page")
    public Page<DressDtos.DressResponse> page(DressDtos.DressFilterRequest request, Pageable pageable) {
        return dressService.page(DressSpecification.filterBy(request), pageable);
    }
    @GetMapping("/list")
    public List<IdName<Long>> list() {
        return dressService.list();
    }
    @GetMapping("/{id}")
    public DressDtos.DressResponse detail(@PathVariable Long id) {
        return dressService.detail(id);
    }

    @PostMapping("/create")
    @PreAuthorize("@securityService.isAtLeast('ADMIN')")
    public DressDtos.DressResponse create(@Valid @RequestBody DressDtos.DressCreateRequest request) {
        return dressService.create(request);
    }
    @PutMapping("/update/{id}")
    @PreAuthorize("@securityService.isAtLeast('ADMIN')")
    public DressDtos.DressResponse update(@PathVariable Long id, @Valid @RequestBody DressDtos.DressUpdateRequest request) {
        if (!id.equals(request.id())) {
            throw new IllegalArgumentException("El ID de la URL no coincide con el ID del cuerpo");
        }
        return dressService.update(request);
    }
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("@securityService.isAtLeast('ADMIN')")
    public void delete(@PathVariable Long id) {
        dressService.delete(id);
    }
}
