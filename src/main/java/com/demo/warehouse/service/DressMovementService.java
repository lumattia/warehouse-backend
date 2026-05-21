package com.demo.warehouse.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.demo.warehouse.domain.DressMovement;
import com.demo.warehouse.mapper.DressMovementDtos.DressMovementCreateRequest;
import com.demo.warehouse.mapper.DressMovementDtos.DressMovementUpdateRequest;
import com.demo.warehouse.repository.DressRepository;
import com.demo.warehouse.repository.DressMovementRepository;

@Service
@RequiredArgsConstructor
public class DressMovementService {
    private final DressMovementRepository dressMovementRepository;
    private final DressRepository dressRepository;

    @Transactional(readOnly = true)
    public Page<DressMovement> page(Specification<DressMovement> spec, Pageable pageable) {
        return dressMovementRepository.getBySpec(spec, pageable);
    }

    @Transactional(readOnly = true)
    public DressMovement detail(Long id) {
        return dressMovementRepository.getByIdOrThrow(id);
    }

    @Transactional
    public DressMovement create(DressMovementCreateRequest request) {
        var dressMovement = new DressMovement();
        var dress = dressRepository.findById(request.dressId()).orElseThrow();
        dress.addStock(request.quantity());
        dressMovement.setDress(dress);
        dressMovement.setQuantity(request.quantity());
        dressMovement.setInstant(request.instant());
        return dressMovementRepository.save(dressMovement);
    }

    @Transactional
    public DressMovement update(DressMovementUpdateRequest request) {
        var dressMovement = dressMovementRepository.getByIdOrThrow(request.id());
        var dress = dressRepository.findById(request.dressId()).orElseThrow();
        dress.setStock(dress.getStock()-dressMovement.getQuantity()+request.quantity());
        dressMovement.setDress(dress);
        dressMovement.setQuantity(request.quantity());
        dressMovement.setInstant(request.instant());
        return dressMovementRepository.save(dressMovement);
    }

    @Transactional
    public void delete(Long toDeleteId) {
        var dressMovement = dressMovementRepository.getByIdOrThrow(toDeleteId);
        var dress = dressMovement.getDress();
        dress.setStock(dress.getStock()-dressMovement.getQuantity());
        dressRepository.save(dress);
        dressMovementRepository.deleteById(toDeleteId);
    }
}
