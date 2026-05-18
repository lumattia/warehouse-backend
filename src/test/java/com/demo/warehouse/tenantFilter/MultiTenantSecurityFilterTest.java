package com.demo.warehouse.tenantFilter;

import com.demo.warehouse.domain.Tenant;
import com.demo.warehouse.domain.User;
import com.demo.warehouse.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MultiTenantSecurityFilterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private UserContextInterceptor interceptor;

    private Tenant tenant;
    private User user;

    @BeforeEach
    void setUp() {
        interceptor = new UserContextInterceptor(userRepository);
        SecurityContextHolder.clearContext();
        
        tenant = new Tenant();
        tenant.setId(UUID.randomUUID());
        tenant.setName("Test Tenant");
        
        user = new User();
        user.setId(1L);
        user.setAuth0Sub("auth0|test123");
        user.setTenant(tenant);
        user.setActiveUserContextId(null);
    }

    @Test
    void doFilterInternal_WhenNotAuthenticated_ShouldProceedWithoutSettingContext() {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(null);

        // Act
        interceptor.afterCompletion(request, response, null, null);
        boolean result = interceptor.preHandle(request, response, null);

        // Assert
        assertTrue(result);
        assertNull(UserContextHolder.get());
        verify(userRepository, never()).findByAuth0Sub(anyString());
    }

    @Test
    void doFilterInternal_WhenAnonymousAuthentication_ShouldProceedWithoutSettingContext() {
        // Arrange
        Authentication auth = new AnonymousAuthenticationToken("key", "anonymous", 
                java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Act
        boolean result = interceptor.preHandle(request, response, null);

        // Assert
        assertTrue(result);
        assertNull(UserContextHolder.get());
        verify(userRepository, never()).findByAuth0Sub(anyString());
    }

    @Test
    void doFilterInternal_WhenAuthenticated_ShouldSetTenantContext() {
        // Arrange
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("auth0|test123");
        when(auth.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByAuth0Sub("auth0|test123")).thenReturn(Optional.of(user));

        // Act
        boolean result = interceptor.preHandle(request, response, null);

        // Assert
        assertTrue(result);
        var context = UserContextHolder.get();
        assertNotNull(context);
        assertEquals(user, context.getRealUser());
        assertEquals(user, context.getEffectiveUser());
    }

    @Test
    void doFilterInternal_WhenAuthenticatedWithEffectiveUser_ShouldSetEffectiveUserContext() {
        // Arrange
        var effectiveUser = new User();
        effectiveUser.setId(2L);
        effectiveUser.setAuth0Sub("auth0|effective");
        effectiveUser.setTenant(tenant);

        user.setActiveUserContextId(2L);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("auth0|test123");
        when(auth.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByAuth0Sub("auth0|test123")).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(effectiveUser));

        // Act
        boolean result = interceptor.preHandle(request, response, null);

        // Assert
        assertTrue(result);
        var context = UserContextHolder.get();
        assertNotNull(context);
        assertEquals(user, context.getRealUser());
        assertEquals(effectiveUser, context.getEffectiveUser());
    }

    @Test
    void doFilterInternal_WhenEffectiveUserFromDifferentTenant_ShouldNotSetEffectiveUser() {
        // Arrange
        var differentTenant = new Tenant();
        differentTenant.setId(UUID.randomUUID());
        differentTenant.setName("Different Tenant");

        var effectiveUser = new User();
        effectiveUser.setId(2L);
        effectiveUser.setAuth0Sub("auth0|effective");
        effectiveUser.setTenant(differentTenant);

        user.setActiveUserContextId(2L);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("auth0|test123");
        when(auth.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByAuth0Sub("auth0|test123")).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(effectiveUser));

        // Act
        boolean result = interceptor.preHandle(request, response, null);

        // Assert
        assertTrue(result);
        var context = UserContextHolder.get();
        assertNotNull(context);
        assertEquals(user, context.getRealUser());
        assertEquals(user, context.getEffectiveUser());
    }

    @Test
    void doFilterInternal_WhenUserNotFound_ShouldThrowRuntimeException() {
        // Arrange
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("auth0|nonexistent");
        when(auth.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByAuth0Sub("auth0|nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> interceptor.preHandle(request, response, null));
    }

    @Test
    void doFilterInternal_ShouldAlwaysClearContextAfterExecution() {
        // Arrange
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("auth0|test123");
        when(auth.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByAuth0Sub("auth0|test123")).thenReturn(Optional.of(user));

        // Act
        interceptor.preHandle(request, response, null);
        interceptor.afterCompletion(request, response, null, null);

        // Assert
        assertNull(UserContextHolder.get());
    }

    @Test
    void doFilterInternal_WhenExceptionThrown_ShouldStillClearContext() {
        // Arrange
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("auth0|test123");
        when(auth.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByAuth0Sub("auth0|test123")).thenReturn(Optional.of(user));

        // Act
        interceptor.preHandle(request, response, null);
        interceptor.afterCompletion(request, response, null, new RuntimeException("Test exception"));

        // Assert
        assertNull(UserContextHolder.get());
    }
}
