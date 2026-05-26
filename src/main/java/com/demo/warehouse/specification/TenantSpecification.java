package com.demo.warehouse.specification;

import org.springframework.data.jpa.domain.Specification;

import com.demo.warehouse.domain.Tenant;
import com.demo.warehouse.domain.User;
import com.demo.warehouse.domain.UserRole;
import com.demo.warehouse.mapper.TenantDtos.TenantFilterRequest;
import com.demo.warehouse.tenantFilter.UserContextHolder;

import java.util.List;
import java.util.UUID;

public class TenantSpecification {
    public static Specification<Tenant> filterBy(TenantFilterRequest filter) {
        User user = UserContextHolder.get().getUser();

        SpecBuilder<Tenant> builder = SpecBuilder.repo(Tenant.class)
            .like("name", filter.name());

        if (user.getRole() == UserRole.RESELLER) {
            List<UUID> allowedTenantIds = user.getAllowedTenants().stream()
                .map(t -> t.getId())
                .toList();
            builder.in("id", allowedTenantIds);
        } else if (user.getRole() == UserRole.ADMIN) {
            builder.equal("id", user.getTenant().getId());
        }

        return builder.build();
    }
}
