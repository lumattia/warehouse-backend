package com.demo.warehouse.tenantFilter;

import com.demo.warehouse.domain.User;
import com.demo.warehouse.domain.UserRole;
import com.demo.warehouse.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class UserContextInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return true;
        }

        String username = auth.getName();
        User realUser = userRepository.findByAuth0Sub(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        UserContext userContext = new UserContext(realUser);
        UserContextHolder.set(userContext);

        // Check for impersonation header
        String impersonateUserId = request.getHeader("X-Impersonate-User");
        if (impersonateUserId != null && !impersonateUserId.isBlank()) {

            // Only ADMIN can impersonate
            if (realUser.getRole() != UserRole.RESELLER && realUser.getRole() != UserRole.SUPERADMIN) {
                throw new RuntimeException("You can't impersonate users.");
            }

            try {
                Long targetUserId = Long.parseLong(impersonateUserId);
                User impersonatedUser = userRepository.findById(targetUserId)
                        .orElseThrow(() -> new RuntimeException("Target user not found: " + targetUserId));

                userContext.setImpersonatedUser(impersonatedUser);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid user ID format in X-Impersonate-User header");
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContextHolder.clear();
    }
}