package com.demo.warehouse.specification;
import org.springframework.data.jpa.domain.Specification;

import com.demo.warehouse.domain.Dress;
import com.demo.warehouse.mapper.DressDtos.DressFilterRequest;

public class DressSpecification {
    public static Specification<Dress> filterBy(DressFilterRequest filters) {
        return SpecBuilder.repo(Dress.class)
        .like("title", filters.title())
        .like("sku", filters.sku())
        .equal("color", filters.color())
        .equal("size", filters.size())
        .greater("price", filters.minPrice())
        .smaller("price", filters.maxPrice())
        .greater("stock", filters.minPrice())
        .smaller("stock", filters.maxPrice())
        .build();
    }
}
