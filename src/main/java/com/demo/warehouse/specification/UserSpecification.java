package com.demo.warehouse.specification;

import org.springframework.data.jpa.domain.Specification;

import com.demo.warehouse.domain.User;
import com.demo.warehouse.mapper.UserDto.UserFilterRequest;

public class UserSpecification {
    public static Specification<User> filterBy(UserFilterRequest filter) {
        return SpecBuilder.repo(User.class)
        .like("username", filter.username())
        .equal("role", filter.role())
        .build();
    }
}
