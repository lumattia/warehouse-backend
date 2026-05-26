package com.demo.warehouse.service;

import java.util.List;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.demo.warehouse.domain.Dress;
import com.demo.warehouse.mapper.DressMapper;
import com.demo.warehouse.mapper.IdName;
import com.demo.warehouse.repository.DressRepository;

import jakarta.annotation.Nonnull;

import com.demo.warehouse.mapper.DressDtos;

@Service
@RequiredArgsConstructor
public class DressService {

    private final DressMapper dressMapper;
    private final DressRepository dressRepository;

    @Transactional(readOnly = true)
    public Page<DressDtos.DressResponse> page(Specification<Dress> spec, @Nonnull Pageable pageable) {
        return dressRepository.getBySpec(spec, pageable).map(dressMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public List<IdName<Long>> list() {
        return dressRepository.getAllAsIdName();
    }
    
    @Transactional(readOnly = true)
    public DressDtos.DressResponse detail(Long id) {
        var dress = dressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dress not found"));
        return dressMapper.toResponse(dress);
    }

    @Transactional
    public DressDtos.DressResponse create(DressDtos.DressCreateRequest request) {
        var dress = new Dress();
        dress.setTitle(request.title());
        dress.setSku(request.sku());
        dress.setSize(request.size());
        dress.setColor(request.color());
        dress.setPrice(request.price());
        dress.setStock(0);
        return dressMapper.toResponse(dressRepository.save(dress));
    }

    @Transactional
    public DressDtos.DressResponse update(DressDtos.DressUpdateRequest request) {
        var dress = dressRepository.getByIdOrThrow(request.id());
        dress.setTitle(request.title());
        dress.setSku(request.sku());
        dress.setSize(request.size());
        dress.setColor(request.color());
        dress.setPrice(request.price());
        dressRepository.save(dress);
        return dressMapper.toResponse(dress);
    }
    
    @Transactional
    public void delete(Long toDeleteId) {
        dressRepository.deleteById(toDeleteId);
    }
}
