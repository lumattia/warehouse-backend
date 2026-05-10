package com.demo.warehouse.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.warehouse.domain.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    List<Tenant> findByExpiresAtBefore(Instant now);
}
