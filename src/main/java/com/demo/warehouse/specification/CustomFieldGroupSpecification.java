package com.demo.warehouse.specification;

import com.demo.warehouse.domain.CustomFieldGroup;
import com.demo.warehouse.domain.ModuleType;
import org.springframework.data.jpa.domain.Specification;

public class CustomFieldGroupSpecification {
    public static Specification<CustomFieldGroup> getAllWithModule(ModuleType module) {
        return SpecBuilder.repo(CustomFieldGroup.class)
        .like("module", module.name())
        .build();
    }
}
