package com.cjh.backend.annotation;

import java.lang.annotation.*;

/**
 * 标记需要进行数据脱敏的方法 (AOP切入点)
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Desensitize {
}