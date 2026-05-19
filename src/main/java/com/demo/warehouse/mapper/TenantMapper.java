package com.demo.warehouse.mapper;

import org.mapstruct.Mapper;

import com.demo.warehouse.domain.Tenant;
import com.demo.warehouse.mapper.TenantDtos.TenantResponse;

@Mapper(componentModel = "spring")
public interface TenantMapper {
    TenantResponse toResponse(Tenant entity);
}
