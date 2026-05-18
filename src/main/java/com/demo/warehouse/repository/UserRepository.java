package com.demo.warehouse.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.demo.warehouse.domain.User;

public interface UserRepository extends BaseRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByAuth0Sub(String auth0Sub);
    List<User> findByTenantId(UUID tenantId);
}
