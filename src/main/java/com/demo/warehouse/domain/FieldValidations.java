package com.demo.warehouse.domain;

import java.util.Collections;
import java.util.List;

public record FieldValidations(
    Boolean required,
    Double min,
    Double max,
    Integer maxLength,
    List<String> options
) {
    public FieldValidations {
        if (required == null) required = false;
        if (options == null) options = Collections.emptyList();
    }
}
