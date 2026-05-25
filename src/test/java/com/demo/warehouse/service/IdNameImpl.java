package com.demo.warehouse.service;

import com.demo.warehouse.mapper.IdName;

public class IdNameImpl implements IdName<Long> {
    private final Long id;
    private final String name;

    public IdNameImpl(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }
}
