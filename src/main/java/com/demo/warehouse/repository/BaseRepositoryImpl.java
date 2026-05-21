package com.demo.warehouse.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.demo.warehouse.domain.TenantScopedEntity;
import com.demo.warehouse.mapper.IdName;
import com.demo.warehouse.tenantFilter.UserContextHolder;

public class BaseRepositoryImpl<T extends TenantScopedEntity, ID> extends SimpleJpaRepository<T, ID> implements BaseRepository<T, ID> {

    private final EntityManager entityManager;
    private final JpaEntityInformation<T, ?> entityInformation;

    public BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        this.entityInformation = entityInformation;
    }

    private UUID getCurrentTenantId() {
        return UserContextHolder.getTenantId();
    }

    private Specification<T> withTenantFilter() {
        return (root, query, cb) -> {
            if (TenantScopedEntity.class.isAssignableFrom(entityInformation.getJavaType())) {
                return cb.equal(root.get("tenant").get("id"), getCurrentTenantId());
            }
            return null;
        };
    }

    private Specification<T> combineWithTenantFilter(Specification<T> spec) {
        if (spec == null) {
            return withTenantFilter();
        }
        return spec.and(withTenantFilter());
    }

    @Override
    @Transactional
    public <S extends T> S save(S entity) {
        if (entity instanceof TenantScopedEntity) {
            var tenantScoped = (TenantScopedEntity) entity;
            if (tenantScoped.getTenant() == null) {
                var tenantId = getCurrentTenantId();
                var tenant = entityManager.find(com.demo.warehouse.domain.Tenant.class, tenantId);
                if (tenant == null) {
                    throw new IllegalArgumentException("Tenant not found with ID: " + tenantId);
                }
                tenantScoped.setTenant(tenant);
            }
        }
        return super.save(entity);
    }

    @Override
    @Transactional
    public <S extends T> List<S> saveAll(Iterable<S> entities) {
        var tenantId = getCurrentTenantId();
        var tenant = entityManager.find(com.demo.warehouse.domain.Tenant.class, tenantId);
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant not found with ID: " + tenantId);
        }

        for (var entity : entities) {
            if (entity instanceof TenantScopedEntity) {
                var tenantScoped = (TenantScopedEntity) entity;
                if (tenantScoped.getTenant() == null) {
                    tenantScoped.setTenant(tenant);
                }
            }
        }
        return super.saveAll(entities);
    }

    @Override
    @Transactional
    public void deleteById(ID id) {
        var entity = getByIdOrThrow(id);
        super.delete(entity);
    }

    @Override
    @Transactional
    public void delete(T entity) {
        if (entity instanceof TenantScopedEntity) {
            var tenantScoped = (TenantScopedEntity) entity;
            var currentTenantId = getCurrentTenantId();
            if (!tenantScoped.getTenant().getId().equals(currentTenantId)) {
                throw new IllegalArgumentException("Cannot delete entity from different tenant");
            }
        }
        super.delete(entity);
    }

    @Override
    public long count() {
        return super.count(withTenantFilter());
    }

    @Override
    public long count(Specification<T> spec) {
        return super.count(combineWithTenantFilter(spec));
    }

    @Override
    public Optional<T> findById(ID id) {
        var entity = super.findById(id);
        if (entity.isPresent() && entity.get() instanceof TenantScopedEntity) {
            var tenantScoped = (TenantScopedEntity) entity.get();
            var currentTenantId = getCurrentTenantId();
            if (!tenantScoped.getTenant().getId().equals(currentTenantId)) {
                return Optional.empty();
            }
        }
        return entity;
    }

    @Override
    public T getByIdOrThrow(ID id) {
        return findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entity not found with ID: " + id + " or not belonging to current tenant"));
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public List<IdName<ID>> getAllAsIdName() {
        var cb = entityManager.getCriteriaBuilder();
        var cq = cb.createQuery(entityInformation.getJavaType());
        var root = cq.from(entityInformation.getJavaType());

        cq.select(root);

        if (TenantScopedEntity.class.isAssignableFrom(entityInformation.getJavaType())) {
            cq.where(cb.equal(root.get("tenant").get("id"), getCurrentTenantId()));
        }

        var entities = entityManager.createQuery(cq).getResultList();

        List<IdName<ID>> result = new java.util.ArrayList<>();
        for (var e : entities) {
            final ID id = (ID) entityManager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(e);
            String tempName;
            try {
                tempName = (String) e.getClass().getMethod("getName").invoke(e);
            } catch (Exception ex) {
                tempName = e.toString();
            }
            final String name = tempName;
            result.add(new IdName<ID>() {
                @Override
                public ID getId() {
                    return id;
                }

                @Override
                public String getName() {
                    return name;
                }
            });
        }
        return result;
    }

    @Override
    public Page<T> getBySpec(Specification<T> spec, Pageable pageable) {
        var combinedSpec = combineWithTenantFilter(spec);
        var totalCount = super.count(combinedSpec);
        
        if (totalCount == 0) {
            return Page.empty(pageable);
        }

        var totalPages = (int) Math.ceil((double) totalCount / pageable.getPageSize());
        
        if (pageable.getPageNumber() >= totalPages) {
            var correctedPageable = PageRequest.of(0, pageable.getPageSize(), pageable.getSort());
            return super.findAll(combinedSpec, correctedPageable);
        }

        return super.findAll(combinedSpec, pageable);
    }
}
