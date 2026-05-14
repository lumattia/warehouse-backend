package com.demo.warehouse.tenantFilter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE}) // Se può usare su metodi o intere classi
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreTenant {
}