package com.demo.warehouse.specification;
import org.springframework.data.jpa.domain.Specification;

public class SpecBuilder<T> {
    private Specification<T> specification;

    private SpecBuilder() {
        this.specification = Specification.where(null);
    }

    public static <T> SpecBuilder<T> repo(Class<T> type) {
        return new SpecBuilder<>();
    }

    public SpecBuilder<T> like(String column, String value) {
        if (value != null && !value.isBlank()) {
            specification = specification.and((root, q, cb) -> 
                cb.like(cb.lower(root.get(column)), "%" + value.toLowerCase() + "%"));
        }
        return this;
    }

    public SpecBuilder<T> equal(String column, Object value) {
        if (value != null) {
            specification = specification.and((root, q, cb) -> cb.equal(root.get(column), value));
        }
        return this;
    }

    public <V extends Comparable<? super V>> SpecBuilder<T> greater(String column, V value) {
        if (value != null) {
            specification = specification.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get(column), value));
        }
        return this;
    }

    public <V extends Comparable<? super V>> SpecBuilder<T> smaller(String column, V value) {
        if (value != null) {
            specification = specification.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get(column), value));
        }
        return this;
    }

    public Specification<T> build() {
        return this.specification;
    }
}