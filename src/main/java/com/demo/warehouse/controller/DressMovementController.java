package com.demo.warehouse.controller;

import jakarta.validation.Valid;
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

import com.demo.warehouse.domain.DressMovement;
import com.demo.warehouse.mapper.DressMovementDtos.DressMovementCreateRequest;
import com.demo.warehouse.mapper.DressMovementDtos.DressMovementFilterRequest;
import com.demo.warehouse.mapper.DressMovementDtos.DressMovementUpdateRequest;
import com.demo.warehouse.service.DressMovementService;
import com.demo.warehouse.specification.DressMovementSpecification;

@RestController
@RequestMapping("/dress-movements")
@RequiredArgsConstructor
@PreAuthorize("@securityService.hasModule('INVENTORY')")
public class DressMovementController {
    private final DressMovementService dressMovementService;

    @GetMapping("/page")
    public Page<DressMovement> page(DressMovementFilterRequest request, Pageable pageable) {
        return dressMovementService.page(DressMovementSpecification.filterBy(request),pageable);
    }

    @GetMapping("/{id}")
    public DressMovement detail(@PathVariable Long id) {
        return dressMovementService.detail(id);
    }

    @PostMapping("/create")
    public DressMovement create(@Valid @RequestBody DressMovementCreateRequest request) {
        return dressMovementService.create(request);
    }

    @PutMapping("/update")
    public DressMovement update(@Valid @RequestBody DressMovementUpdateRequest request) {
        return dressMovementService.update(request);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        dressMovementService.delete(id);
    }
}
