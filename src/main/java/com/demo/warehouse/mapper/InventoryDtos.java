package com.demo.warehouse.mapper;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
public class InventoryDtos {
    
    public record InventoryCreateRequest(
        @NotNull
        Long dressId,
        @NotNull
        Integer quantity,
        Instant instant
    ) {
        public InventoryCreateRequest {
            if (instant == null) {
                instant = Instant.now();
            }
        }
    }

    public record InventoryUpdateRequest(
        @NotNull
        Long id,
        @NotNull
        Long dressId,
        @NotNull
        Integer quantity,
        @NotNull
        Instant instant
    ) {}

    // What you use for a filtered search
    public record InventoryFilterRequest(
        String title,
        String sku,
        String color,
        String size,
        Integer minQuantity,
        Integer maxQuantity
    ) {}
    @Builder
    public record DressResponse(
        Long id,
        String name,
        String sku,
        String size,
        String color,
        BigDecimal price
    ) {}

}