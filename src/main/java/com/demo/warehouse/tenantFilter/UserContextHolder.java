package com.demo.warehouse.tenantFilter;

import java.util.UUID;

import com.demo.warehouse.domain.Tenant;

public final class UserContextHolder {
    private static final ThreadLocal<UserContext> HOLDER = new ThreadLocal<>();

    private UserContextHolder() {
    }

    public static void set(UserContext securityContext) {
        HOLDER.set(securityContext);
    }
    public static UserContext get() {
        return HOLDER.get();
    }

    public static Tenant getTenant() {
        var context = UserContextHolder.get();
        if (context == null) {
            throw new NullPointerException("User context not set. Cannot determine tenant.");
        }
        return context.getUser().getTenant();
    }
    public static UUID getTenantId() {
        return getTenant().getId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
