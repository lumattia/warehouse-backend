package com.demo.warehouse.tenantFilter;

import com.demo.warehouse.domain.User;
import com.demo.warehouse.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class UserContextInterceptor implements HandlerInterceptor { // <-- Cambia la interfaz

    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return true; // Continúa la petición de forma anónima
        }

        String username = auth.getName();
        User realUser = userRepository.findByAuth0Sub(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Optional<User> effectiveUser = resolveEffectiveUser(realUser);
        UserContextHolder.set(UserContext.builder()
                .realUser(realUser)
                .effectiveUser(effectiveUser)
                .build());

        return true; // Permite que la petición avance al Controlador
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // Este es el equivalente al bloque 'finally': limpia el ThreadLocal al terminar la petición
        UserContextHolder.clear();
    }

    private Optional<User> resolveEffectiveUser(User realUser) {
        Long activeUserContextId = realUser.getActiveUserContextId();
        if (activeUserContextId == null || activeUserContextId.equals(realUser.getId())) {
            return Optional.empty();
        }
        return userRepository.findById(activeUserContextId)
                .filter(effective -> effective.getTenant().getId().equals(realUser.getTenant().getId()));
    }
}