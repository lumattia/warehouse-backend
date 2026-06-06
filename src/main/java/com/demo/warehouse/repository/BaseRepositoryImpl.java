package com.demo.warehouse.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.demo.warehouse.domain.TenantScopedEntity;
import com.demo.warehouse.mapper.IdName;
import com.demo.warehouse.mapper.IdNameImpl;
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
    @NullMarked
    public <S extends T> S save(S entity) {
        var tenantScoped = (TenantScopedEntity) entity;
        if (tenantScoped.getTenant() == null) {
            var tenant = UserContextHolder.getTenant();
            tenantScoped.setTenant(tenant);
        }
        return super.save(entity);
    }

    @Override
    @Transactional
    @NullMarked
    public <S extends T> List<S> saveAll(Iterable<S> entities) {
        var tenant = UserContextHolder.getTenant();

        for (var entity : entities) {
            var tenantScoped = (TenantScopedEntity) entity;
            if (tenantScoped.getTenant() == null) {
                tenantScoped.setTenant(tenant);
            }
        }
        return super.saveAll(entities);
    }

    @Override
    @Transactional
    public void deleteById(@Nullable ID id) {
        var entity = getByIdOrThrow(id);
        super.delete(entity);
    }

    @Override
    @Transactional
    public void delete(@NonNull T entity) {
        var tenantScoped = (TenantScopedEntity) entity;
        var currentTenantId = getCurrentTenantId();
        if (!tenantScoped.getTenant().getId().equals(currentTenantId)) {
            throw new IllegalArgumentException("Cannot delete entity from different tenant");
        }
        super.delete(entity);
    }

    @Override
    public long count() {
        return super.count(withTenantFilter());
    }

    @Override
    @NullMarked
    public long count(Specification<T> spec) {
        return super.count(combineWithTenantFilter(spec));
    }

    @Override
    @NullMarked
    public Optional<T> findById(@Nullable ID id) {
        if (id == null) {
            return Optional.empty();
        }
        var entity = super.findById(id);
        if (entity.isPresent()) {
            var tenantScoped = (TenantScopedEntity) entity.get();
            var currentTenantId = getCurrentTenantId();
            if (!tenantScoped.getTenant().getId().equals(currentTenantId)) {
                return Optional.empty();
            }
        }
        return entity;
    }

    @Override
    public T getByIdOrThrow(@Nullable ID id) {
        return findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entity not found with ID: " + id + " or not belonging to current tenant"));
    }
    public Optional<T> getBySpec(Specification<T> spec) {
        var combinedSpec = combineWithTenantFilter(spec);
        return super.findOne(combinedSpec);
    }
    @Override
    @SuppressWarnings({ "unchecked" })
    public List<IdName<ID>> getAllAsIdName() {
        var cb = entityManager.getCriteriaBuilder();
        var cq = cb.createQuery(IdNameImpl.class);
        var root = cq.from(entityInformation.getJavaType());

        cq.select(cb.construct(IdNameImpl.class, root.get("id"), root.get("name")));

        if (TenantScopedEntity.class.isAssignableFrom(entityInformation.getJavaType())) {
            cq.where(cb.equal(root.get("tenant").get("id"), getCurrentTenantId()));
        }
        cq.orderBy(cb.asc(root.get("name")));
        return (List<IdName<ID>>) (List<?>) entityManager.createQuery(cq).getResultList();
    }

    public List<T> listBySpec(Specification<T> spec) {
        var combinedSpec = combineWithTenantFilter(spec);
        return super.findAll(combinedSpec);
    }
    public Page<T> pageBySpec(Specification<T> spec, Pageable pageable) {
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
