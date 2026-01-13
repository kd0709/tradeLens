package com.cjh.backend.common;

import java.lang.annotation.*;

/**
 * 当前登录用户注解
 * 使用方式：在 Controller 方法参数上加 @CurrentUser Long userId
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {

}