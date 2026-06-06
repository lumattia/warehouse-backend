package com.demo.warehouse.tenantFilter;

import com.demo.warehouse.domain.User;

public class UserContext {
    private final User realUser;
    private User impersonatedUser;
    
    public UserContext(User realUser) {
        this.realUser = realUser;
    }

    public User getRealUser() {
        return realUser;
    }

    public User getUser() {
        return impersonatedUser != null ? impersonatedUser : realUser;
    }

    public void setImpersonatedUser(User impersonatedUser) {
        this.impersonatedUser = impersonatedUser;
    }
}
