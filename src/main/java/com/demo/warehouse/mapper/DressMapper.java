package com.demo.warehouse.mapper;

import org.springframework.stereotype.Component;
import com.demo.warehouse.domain.Dress;
import com.demo.warehouse.mapper.DressDtos.DressResponse;

@Component
public class DressMapper {
    public DressResponse toResponse(Dress entity) {
        if (entity == null) return null;
        
        return DressResponse.builder()
            .id(entity.getId())
            .name(entity.getTitle())
            .sku(entity.getSku())
            .size(entity.getSize())
            .color(entity.getColor())
            .price(entity.getPrice())
            .build();
    }
}