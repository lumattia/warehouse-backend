package com.demo.warehouse.controller;

import com.demo.warehouse.domain.ModuleType;
import com.demo.warehouse.domain.UserRole;
import com.demo.warehouse.tenantFilter.UserContextHolder;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/enums")
public class EnumController {

    @GetMapping("/roles")
    public List<String> getAssignableRoles() {
        UserRole currentRole = UserContextHolder.get().getUser().getRole();

        return switch (currentRole) {
            case SUPERADMIN -> List.of("SUPERADMIN", "RESELLER", "ADMIN", "USER");
            case RESELLER -> List.of("ADMIN", "USER");
            case ADMIN -> List.of("ADMIN", "USER");
            case USER -> List.of();
        };
    }
    @GetMapping("/modules")
    public List<ModuleType> getAssignableModules() {
        return UserContextHolder.get().getUser().getTenant().getModules().stream().sorted().toList();
    }
}
