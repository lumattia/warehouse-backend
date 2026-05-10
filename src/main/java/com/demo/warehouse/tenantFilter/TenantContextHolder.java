package com.demo.warehouse.tenantFilter;

import com.demo.warehouse.domain.Tenant;

public final class TenantContextHolder {
    private static final ThreadLocal<TenantContext> HOLDER = new ThreadLocal<>();

    private TenantContextHolder() {
    }

    public static void set(TenantContext securityContext) {
        HOLDER.set(securityContext);
    }
    public static TenantContext get() {
        return HOLDER.get();
    }

    public static Tenant getTenant() {
        return get().getEffectiveUser().getTenant();
    }
    public static Long getTenantId() {
        return getTenant().getId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
