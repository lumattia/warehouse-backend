package com.demo.warehouse.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.warehouse.domain.Inventory;
import com.demo.warehouse.mapper.InventoryDtos.InventoryCreateRequest;
import com.demo.warehouse.mapper.InventoryDtos.InventoryFilterRequest;
import com.demo.warehouse.mapper.InventoryDtos.InventoryUpdateRequest;
import com.demo.warehouse.service.InventoryService;
import com.demo.warehouse.specification.InventorySpecification;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @GetMapping("/page")
    public Page<Inventory> page(InventoryFilterRequest request, Pageable pageable) {
        return inventoryService.page(InventorySpecification.filterBy(request),pageable);
    }

    @PostMapping("/create")
    public Inventory create(@Valid @RequestBody InventoryCreateRequest request) {
        return inventoryService.create(request);
    }
    @PutMapping("/update")
    public Inventory update(@Valid @RequestBody InventoryUpdateRequest request) {
        return inventoryService.update(request);
    }
    @DeleteMapping("/delete")
    public void delete(Long toDeleteId) {
        inventoryService.delete(toDeleteId);
    }
}
