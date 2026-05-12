package com.demo.warehouse.mapper;

import lombok.Data;

@Data
public class IdNameImpl<T> implements IdName<T> {
    private T id;
    private String name;
    // 构造器、getter、setter
}