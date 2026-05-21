package com.demo.warehouse.mapper;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
public class DressMovementDtos {
    
    public record DressMovementCreateRequest(
        @NotNull
        Long dressId,
        @NotNull
        Integer quantity,
        Instant instant
    ) {
        public DressMovementCreateRequest {
            if (instant == null) {
                instant = Instant.now();
            }
        }
    }

    public record DressMovementUpdateRequest(
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
    public record DressMovementFilterRequest(
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