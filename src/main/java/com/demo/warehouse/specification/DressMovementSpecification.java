package com.demo.warehouse.specification;
import org.springframework.data.jpa.domain.Specification;

import com.demo.warehouse.domain.DressMovement;
import com.demo.warehouse.mapper.DressMovementDtos.DressMovementFilterRequest;

public class DressMovementSpecification {
    public static Specification<DressMovement> filterBy(DressMovementFilterRequest filters) {
        return SpecBuilder.repo(DressMovement.class)
        .like("dress.title", filters.title())
        .like("dress.sku", filters.sku())
        .equal("dress.color", filters.color())
        .equal("dress.size", filters.size())
        .greater("quantity", filters.minQuantity())
        .smaller("quantity", filters.maxQuantity())
        .build();
    }
}
