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
public class UserContextInterceptor implements HandlerInterceptor { // <-- Change the interface

    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return true; // Continue the request anonymously
        }

        String username = auth.getName();
        User realUser = userRepository.findByAuth0Sub(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Optional<User> effectiveUser = resolveEffectiveUser(realUser);
        UserContextHolder.set(UserContext.builder()
                .realUser(realUser)
                .effectiveUser(effectiveUser)
                .build());

        return true; // Allow the request to proceed to the Controller
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // This is equivalent to a 'finally' block: clears the ThreadLocal when the request completes
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