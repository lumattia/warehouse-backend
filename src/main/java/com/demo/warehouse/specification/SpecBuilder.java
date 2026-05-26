package com.demo.warehouse.specification;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

public class SpecBuilder<T> {
    private Specification<T> specification;

    private SpecBuilder() {
    this.specification = (root, query, cb) -> cb.conjunction();
    }

    public static <T> SpecBuilder<T> repo(Class<T> type) {
        return new SpecBuilder<>();
    }

    @SuppressWarnings("unchecked")
    private <Y> Path<Y> getPath(Root<T> root, String columnPath) {
        String[] parts = columnPath.split("\\.");
        Path<?> path = root;
        for (String part : parts) {
            path = path.get(part);
        }
        return (Path<Y>) path;
    }

    public SpecBuilder<T> like(String column, String value) {
        if (value != null && !value.isBlank()) {
            specification = specification.and((root, q, cb) -> 
                cb.like(cb.lower(getPath(root, column)), "%" + value.toLowerCase() + "%"));
        }
        return this;
    }

    public SpecBuilder<T> equal(String column, Object value) {
        if (value != null) {
            specification = specification.and((root, q, cb) -> {
                if (value instanceof String) {
                    return cb.equal(cb.lower(getPath(root, column)), ((String) value).toLowerCase());
                }
                return cb.equal(getPath(root, column), value);
            });
        }
        return this;
    }

    public <V extends Comparable<? super V>> SpecBuilder<T> greater(String column, V value) {
        if (value != null) {
            specification = specification.and((root, q, cb) -> 
                cb.greaterThanOrEqualTo(getPath(root, column), value));
        }
        return this;
    }

    public <V extends Comparable<? super V>> SpecBuilder<T> smaller(String column, V value) {
        if (value != null) {
            specification = specification.and((root, q, cb) -> 
                cb.lessThanOrEqualTo(getPath(root, column), value));
        }
        return this;
    }

    public Specification<T> build() {
        return this.specification;
    }
}