package com.cjh.backend.annotation;

import com.cjh.backend.common.DesensitizeType;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DesensitizeField {
    DesensitizeType type();
}