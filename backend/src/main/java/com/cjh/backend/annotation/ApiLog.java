package com.cjh.backend.annotation;


import java.lang.annotation.*;


/**
 * 接口访问日志与耗时统计注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiLog {
    /**
     * 接口功能描述，例如："获取商品列表"
     */
    String value() default "";

}
