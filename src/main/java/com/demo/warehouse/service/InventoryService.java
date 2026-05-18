package com.demo.warehouse.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.demo.warehouse.domain.Inventory;
import com.demo.warehouse.mapper.InventoryDtos.InventoryCreateRequest;
import com.demo.warehouse.mapper.InventoryDtos.InventoryUpdateRequest;
import com.demo.warehouse.repository.DressRepository;
import com.demo.warehouse.repository.InventoryRepository;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final DressRepository dressRepository;
    
    @Transactional(readOnly = true)
    public Page<Inventory> page(Specification<Inventory> spec, Pageable pageable) {
        return inventoryRepository.getBySpec(spec, pageable);
    }

    @Transactional
    public Inventory create(InventoryCreateRequest request) {
        var inventory = new Inventory();
        var dress = dressRepository.findById(request.dressId()).orElseThrow();
        dress.addStock(request.quantity());
        inventory.setDress(dress);
        inventory.setQuantity(request.quantity());
        inventory.setInstant(request.instant());
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory update(InventoryUpdateRequest request) {
        var inventory = inventoryRepository.getByIdOrThrow(request.id());
        var dress = dressRepository.findById(request.dressId()).orElseThrow();
        dress.setStock(dress.getStock()-inventory.getQuantity()+request.quantity());
        inventory.setDress(dress);
        inventory.setQuantity(request.quantity());
        inventory.setInstant(request.instant());
        return inventoryRepository.save(inventory);
    }
    
    @Transactional
    public void delete(Long toDeleteId) {
        var inventory = inventoryRepository.getByIdOrThrow(toDeleteId);
        var dress = inventory.getDress();
        dress.setStock(dress.getStock()-inventory.getQuantity());
        dressRepository.save(dress);
        inventoryRepository.deleteById(toDeleteId);
    }
}
