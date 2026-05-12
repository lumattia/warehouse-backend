package com.demo.warehouse.tenantFilter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.demo.warehouse.domain.User;
import com.demo.warehouse.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class MultiTenantSecurityFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Filter tenantFilter = null;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User realUser;

            if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
                filterChain.doFilter(request, response);
                return;
            }

            String username = auth.getName();
            realUser = userRepository.findByAuth0Sub(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            Optional<User> effectiveUser = resolveEffectiveUser(realUser);

            TenantContextHolder.set(TenantContext.builder()
                    .realUser(realUser)
                    .effectiveUser(effectiveUser)
                    .build());

            Session session = entityManager.unwrap(Session.class);
            tenantFilter = session.enableFilter("tenantFilter");
            tenantFilter.setParameter("tenantId", TenantContextHolder.get().getEffectiveUser().getTenant().getId());

            filterChain.doFilter(request, response);
        } finally {
            if (tenantFilter != null) {
                entityManager.unwrap(Session.class).disableFilter("tenantFilter");
            }
            TenantContextHolder.clear();
        }
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
