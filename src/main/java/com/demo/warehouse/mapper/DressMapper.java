package com.demo.warehouse.mapper;

import org.mapstruct.Mapper;
import com.demo.warehouse.domain.Dress;
import com.demo.warehouse.mapper.DressDtos.DressResponse;

@Mapper(componentModel = "spring")
public interface DressMapper {
    DressResponse toResponse(Dress entity);
}