package com.demo.warehouse.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.demo.warehouse.domain.Dress;
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
        return inventoryRepository.findAll(spec, pageable);
    }

    @Transactional
    public Inventory create(InventoryCreateRequest request) {
        Inventory inventory = new Inventory();
        Dress dress = dressRepository.getReferenceById(request.dressId());
        dress.addStock(request.quantity());
        inventory.setTenant(dress.getTenant());
        inventory.setDress(dress);
        inventory.setQuantity(request.quantity());
        inventory.setInstant(request.instant());
        dressRepository.save(dress);
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory update(InventoryUpdateRequest request) {
        Inventory inventory = inventoryRepository.getReferenceById(request.id());
        Dress dress = dressRepository.getReferenceById(request.dressId());
        dress.setStock(dress.getStock()-inventory.getQuantity()+request.quantity());
        inventory.setDress(dress);
        inventory.setQuantity(request.quantity());
        inventory.setInstant(request.instant());
        dressRepository.save(dress);
        return inventoryRepository.save(inventory);
    }
    @Transactional
    public void delete(Long toDeleteId) {
        Inventory inventory = inventoryRepository.getReferenceById(toDeleteId);
        Dress dress = inventory.getDress();
        dress.setStock(dress.getStock()-inventory.getQuantity());
        dressRepository.save(dress);
        inventoryRepository.deleteById(toDeleteId);
    }
}
