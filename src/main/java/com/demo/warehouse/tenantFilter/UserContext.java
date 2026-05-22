package com.demo.warehouse.tenantFilter;

import com.demo.warehouse.domain.User;

import lombok.Builder;

@Builder
public class UserContext {
    private final User realUser;
    
    public UserContext(User realUser) {
        this.realUser = realUser;
    }

    public User getUser() {
        return realUser;
    }
}
