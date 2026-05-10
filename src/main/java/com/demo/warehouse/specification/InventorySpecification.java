package com.demo.warehouse.specification;
import org.springframework.data.jpa.domain.Specification;

import com.demo.warehouse.domain.Inventory;
import com.demo.warehouse.mapper.InventoryDtos.InventoryFilterRequest;

public class InventorySpecification {
    public static Specification<Inventory> filterBy(InventoryFilterRequest filters) {
        return SpecBuilder.repo(Inventory.class)
        .like("dress.titulo", filters.title())
        .like("dress.sku", filters.sku())
        .equal("dress.color", filters.color())
        .equal("dress.size", filters.size())
        .greater("quantity", filters.minQuantity())
        .smaller("quantity", filters.maxQuantity())
        .build();
    }
}
