package com.demo.warehouse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.warehouse.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByAuth0Sub(String auth0Sub);
    Optional<User> findFirstByTenantId(Long tenantId);
    List<User> findByTenantId(Long tenantId);
}
