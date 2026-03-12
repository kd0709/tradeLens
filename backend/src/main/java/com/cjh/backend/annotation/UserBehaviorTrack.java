package com.cjh.backend.annotation;

import java.lang.annotation.*;

/**
 * 用户行为轨迹埋点注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UserBehaviorTrack {
    /**
     * 行为类型描述（如：VIEW, FAVORITE, CART, BUY）
     */
    String action() default "VIEW";

    /**
     * 该行为增加的偏好分数
     */
    int score() default 1;
}