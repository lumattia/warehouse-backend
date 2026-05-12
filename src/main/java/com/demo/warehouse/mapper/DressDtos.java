package com.demo.warehouse.mapper;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
public class DressDtos {
    
    public static record DressCreateRequest(
        @NotBlank
        String title,
        @NotBlank
        String sku,
        @NotNull
        String size,
        @NotNull
        String color,
        @Positive
        @NotNull
        BigDecimal price
    ) {}

    public record DressUpdateRequest(
        @NotNull
        Long id,
        @NotBlank
        String title,
        @NotBlank
        String sku,
        @NotNull
        String size,
        @NotNull
        String color,
        @Positive
        @NotNull
        BigDecimal price
    ) {}

    // Lo que usas para una búsqueda filtrada
    public static record DressFilterRequest(
        String title,
        String sku,
        String color,
        String size,
        Integer minStock,
        Integer maxStock,
        BigDecimal minPrice,
        BigDecimal maxPrice
    ) {}
    @Builder
    public static record DressResponse(
        Long id,
        String title,
        String sku,
        String size,
        String color,
        BigDecimal price,
        Integer stock
    ) {}

}