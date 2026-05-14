package com.demo.warehouse.tenantFilter;

import jakarta.persistence.EntityManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantFilterAspect {

    private final EntityManager entityManager;

    public TenantFilterAspect(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

@Around("@within(org.springframework.stereotype.Service) && !@annotation(com.demo.warehouse.annotations.IgnoreTenant) && !@within(com.demo.warehouse.annotations.IgnoreTenant)")
    public Object enableTenantFilter(ProceedingJoinPoint joinPoint) throws Throwable {
        var session = entityManager.unwrap(Session.class);
        var tenantId = TenantContextHolder.get().getEffectiveUser().getTenant().getId();
        
        session.enableFilter("tenantFilter")
               .setParameter("tenantId", tenantId);
        
        try {
            return joinPoint.proceed();
        } finally {
            session.disableFilter("tenantFilter");
        }
    }
}
