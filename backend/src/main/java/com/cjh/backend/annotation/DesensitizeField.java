package com.cjh.backend.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DesensitizeField {
    DesensitizeType type();
}