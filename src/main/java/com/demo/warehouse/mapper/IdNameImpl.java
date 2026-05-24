package com.demo.warehouse.mapper;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IdNameImpl<T> implements IdName<T> {
    private T id;
    private String name;
}
