package com.demo.warehouse.service;

import java.util.List;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.demo.warehouse.domain.Dress;
import com.demo.warehouse.mapper.DressMapper;
import com.demo.warehouse.mapper.IdName;
import com.demo.warehouse.mapper.DressDtos;
import com.demo.warehouse.repository.DressRepository;
import com.demo.warehouse.tenantFilter.TenantContextHolder;

@Service
@RequiredArgsConstructor
public class DressService {

    private final DressMapper dressMapper;
    private final DressRepository dressRepository;

    @Transactional(readOnly = true)
    public Page<DressDtos.DressResponse> page(Specification<Dress> spec, @NonNull Pageable pageable) {
        return dressRepository.findAll(spec, pageable)
                .map(dressMapper::toResponse);
    }
    @Transactional(readOnly = true)
    public List<IdName> list() {
        return dressRepository.findAllProjectedBy(IdName.class);
    }

    @Transactional
    public DressDtos.DressResponse create(DressDtos.DressCreateRequest request) {
        Dress dress = new Dress();
        dress.setTenant(TenantContextHolder.getTenant());
        dress.setTitle(request.title());
        dress.setSku(request.sku());
        dress.setSize(request.size());
        dress.setColor(request.color());
        dress.setPrice(request.price());
        return dressMapper.toResponse(dressRepository.save(dress));
    }

    @Transactional
    public DressDtos.DressResponse update(DressDtos.DressUpdateRequest request) {
        Dress dress = dressRepository.getReferenceById(request.id());
        dress.setTitle(request.title());
        dress.setSku(request.sku());
        dress.setSize(request.size());
        dress.setColor(request.color());
        dress.setPrice(request.price());
        return dressMapper.toResponse(dressRepository.save(dress));
    }
    @Transactional
    public void delete(Long toDeleteId) {
        dressRepository.deleteById(toDeleteId);
    }
}
