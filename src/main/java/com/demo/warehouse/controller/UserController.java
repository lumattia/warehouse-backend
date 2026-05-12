package com.demo.warehouse.controller;

import com.demo.warehouse.domain.ModuleType;
import com.demo.warehouse.domain.Tenant;
import com.demo.warehouse.domain.User;
import com.demo.warehouse.domain.UserRole;
import com.demo.warehouse.mapper.UserDto;
import com.demo.warehouse.mapper.UserMapper;
import com.demo.warehouse.repository.TenantRepository;
import com.demo.warehouse.repository.UserRepository;
import com.demo.warehouse.service.Auth0ManagementService;
import com.demo.warehouse.tenantFilter.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final Auth0ManagementService auth0ManagementService;
    private final UserMapper userMapper;
    @GetMapping("/me")
    public UserDto.LoggedUserDto getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return userMapper.toLogged(TenantContextHolder.get().getEffectiveUser());
    }

    @PostMapping("/demo")
    public Map<String, String> createDemoUser() throws Exception {
        String randomId = UUID.randomUUID().toString().substring(0, 8);
        String email = "demo_" + randomId + "@example.com";
        String password = "Demo" + randomId + "!";
        
        // Creamos el usuario en Auth0
        String auth0Sub = auth0ManagementService.createUser(email, password);
        
        // Registramos en nuestra DB local
        registerNewUser(email, auth0Sub);
        
        return Map.of(
            "email", email,
            "password", password,
            "message", "¡Cuenta de Demo creada! Ahora puedes iniciar sesión con estas credenciales."
        );
    }

    private User registerNewUser(String username, String auth0Sub) {
        // Para la demo, creamos un nuevo tenant temporal para cada nuevo usuario
        Tenant tenant = new Tenant();
        tenant.setName("Demo " + UUID.randomUUID().toString().substring(0, 8));
        tenant.setModules(Set.of(ModuleType.DRESS, ModuleType.INVENTORY));
        tenant = tenantRepository.save(tenant);

        User user = new User();
        user.setUsername(username);
        user.setAuth0Sub(auth0Sub);
        user.setTenant(tenant);
        user.setRole(UserRole.ADMIN);
        return userRepository.save(user);
    }
}
