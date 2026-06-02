package com.demo.warehouse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import com.demo.warehouse.domain.TenantScopedEntity;
import com.demo.warehouse.mapper.IdName;

@NoRepositoryBean
public interface BaseRepository<T extends TenantScopedEntity, ID> extends Repository<T, ID> {

    /**
     * Save entity with automatic tenant injection
     */
    <S extends T> S save(S entity);

    /**
     * Save all entities with automatic tenant injection
     */
    <S extends T> List<S> saveAll(Iterable<S> entities);

    /**
     * Delete entity ensuring it belongs to current tenant
     */
    void deleteById(ID id);

    /**
     * Delete entity ensuring it belongs to current tenant
     */
    void delete(T entity);

    /**
     * Count records applying mandatory tenant filter
     */
    long count();

    /**
     * Count records applying mandatory tenant filter with specification
     */
    long count(Specification<T> spec);

    /**
     * Find entity by ID ensuring it belongs to current tenant
     */
    Optional<T> findById(ID id);

    /**
     * Find entity by ID or throw exception if not found or not belonging to current tenant
     */
    T getByIdOrThrow(ID id);
    Optional<T> getBySpec(Specification<T> spec);

    /**
     * Return projected IdName list filtered by tenant
     */
    List<IdName<ID>> getAllAsIdName();
    List<T> listBySpec(Specification<T> spec);

    /**
     * Find entities with specification and mandatory tenant filter
     * Auto-corrects pagination if requested page is out of range
     */
    Page<T> pageBySpec(Specification<T> spec, Pageable pageable);
}
