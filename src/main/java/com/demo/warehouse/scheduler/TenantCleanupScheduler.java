package com.demo.warehouse.scheduler;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.demo.warehouse.domain.Tenant;
import com.demo.warehouse.domain.User;
import com.demo.warehouse.repository.TenantRepository;
import com.demo.warehouse.repository.UserRepository;
import com.demo.warehouse.service.Auth0ManagementService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantCleanupScheduler {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final Auth0ManagementService auth0ManagementService;
    private final EntityManager entityManager;

    @Transactional
    @Scheduled(cron = "0 * * * * *")
    public void cleanupExpiredTenants() {
        List<Tenant> expiredTenants = tenantRepository.findByExpiresAtBefore(Instant.now());
        if (expiredTenants.isEmpty()) {
            return;
        }

        for (Tenant tenant : expiredTenants) {
            // Delete associated users from Auth0 if they have sub
            List<User> users = userRepository.findByTenantId(tenant.getId());
            for (User user : users) {
                if (user.getAuth0Sub() != null && user.getAuth0Sub().startsWith("auth0|")) {
                    auth0ManagementService.deleteUser(user.getAuth0Sub());
                }
                entityManager.detach(user);
            }
        }
        tenantRepository.deleteAll(expiredTenants);

        log.info("Removed {} expired tenants and their Auth0 users", expiredTenants.size());
    }
}
