package com.demo.warehouse.tenantFilter;

import java.util.Optional;

import com.demo.warehouse.domain.User;

import lombok.Builder;

@Builder
public class UserContext {
    private final User realUser;
    private final Optional<User> effectiveUser;

    public UserContext(User realUser, Optional<User> effectiveUser) {
        this.realUser = realUser;
        this.effectiveUser = effectiveUser;
    }

    public User getRealUser() {
        return realUser;
    }
    public User getEffectiveUser() {
        return effectiveUser.orElse(realUser);
    }
}
