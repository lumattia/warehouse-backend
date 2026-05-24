package com.demo.warehouse.service;

import org.springframework.stereotype.Component;

import com.demo.warehouse.domain.UserRole;
import com.demo.warehouse.tenantFilter.UserContextHolder;

@Component("securityService")
public class SecurityService {

    private static final int ROLE_HIERARCHY_SUPERADMIN = 4;
    private static final int ROLE_HIERARCHY_RESELLER = 3;
    private static final int ROLE_HIERARCHY_ADMIN = 2;
    private static final int ROLE_HIERARCHY_USER = 1;

    public boolean hasAnyRole(String... roles) {
        UserRole currentRole = UserContextHolder.get().getUser().getRole();
        for (String role : roles) {
            if (currentRole.name().equals(role)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAtLeast(String role) {
        UserRole currentRole = UserContextHolder.get().getUser().getRole();
        int currentLevel = getRoleLevel(currentRole);
        int requiredLevel = getRoleLevel(UserRole.valueOf(role));
        return currentLevel >= requiredLevel;
    }

    public boolean hasModule(String moduleName) {
        return UserContextHolder.get().getUser().getTenant().getModules().stream()
                .anyMatch(m -> m.name().equals(moduleName));
    }

    public boolean canManageTenants() {
        UserRole currentRole = UserContextHolder.get().getUser().getRole();
        if (currentRole == UserRole.SUPERADMIN) {
            return true;
        }
        if (currentRole == UserRole.RESELLER) {
            return !UserContextHolder.get().getUser().getAllowedTenants().isEmpty();
        }
        return false;
    }

    private int getRoleLevel(UserRole role) {
        return switch (role) {
            case SUPERADMIN -> ROLE_HIERARCHY_SUPERADMIN;
            case RESELLER -> ROLE_HIERARCHY_RESELLER;
            case ADMIN -> ROLE_HIERARCHY_ADMIN;
            case USER -> ROLE_HIERARCHY_USER;
        };
    }
}
