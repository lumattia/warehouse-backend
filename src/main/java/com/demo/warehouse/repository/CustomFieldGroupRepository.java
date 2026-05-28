package com.demo.warehouse.repository;

import com.demo.warehouse.domain.CustomFieldGroup;
import com.demo.warehouse.domain.ModuleType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomFieldGroupRepository extends JpaRepository<CustomFieldGroup, Long> {
    List<CustomFieldGroup> findByTenantIdAndModuleOrderByGroupOrderAsc(UUID tenantId, ModuleType module);
    void deleteByTenantId(UUID tenantId);
}
