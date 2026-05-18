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
import com.demo.warehouse.service.UserService;
import com.demo.warehouse.tenantFilter.UserContextHolder;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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
    private final UserService userService;

    @GetMapping("/me")
    public UserDto.LoggedUserDto getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return userMapper.toLogged(UserContextHolder.get().getEffectiveUser());
    }

    @PostMapping("/demo")
    public Map<String, String> createDemoUser() throws Exception {
        String randomId = UUID.randomUUID().toString().substring(0, 8);
        String email = "demo_" + randomId + "@example.com";
        String password = "Demo" + randomId + "!";
        
        String auth0Sub = auth0ManagementService.createUser(email, password);
        registerNewUser(email, auth0Sub);
        
        return Map.of(
            "email", email,
            "password", password,
            "message", "¡Cuenta de Demo creada! Ahora puedes iniciar sesión con estas credenciales."
        );
    }

    @GetMapping("/page")
    public Page<UserDto.UserResponse> page(UserDto.UserFilterRequest filter, Pageable pageable) {
        Specification<User> spec = buildSpecification(filter);
        return userService.page(spec, pageable);
    }

    @GetMapping("/list")
    public List<com.demo.warehouse.mapper.IdName<Long>> list() {
        return userService.list();
    }

    @GetMapping("/{id}")
    public UserDto.UserResponse detail(@PathVariable Long id) {
        return userService.detail(id);
    }

    @PostMapping("/create")
    public UserDto.UserResponse create(@RequestBody UserDto.UserCreateRequest request) {
        return userService.create(request);
    }

    @PutMapping("/update/{id}")
    public UserDto.UserResponse update(@PathVariable Long id, @RequestBody UserDto.UserUpdateRequest request) {
        return userService.update(request);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }

    private User registerNewUser(String username, String auth0Sub) {
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

    private Specification<User> buildSpecification(UserDto.UserFilterRequest filter) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            
            if (filter != null) {
                if (filter.username() != null && !filter.username().isEmpty()) {
                    predicates.add(cb.like(cb.lower(root.get("username")), "%" + filter.username().toLowerCase() + "%"));
                }
                if (filter.role() != null) {
                    predicates.add(cb.equal(root.get("role"), filter.role()));
                }
            }
            
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
