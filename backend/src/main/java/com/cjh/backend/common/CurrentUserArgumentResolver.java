package com.cjh.backend.common;

import com.cjh.backend.exception.BusinessException;
import com.cjh.backend.exception.ErrorConstants;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 当前用户参数解析器
 * 自动从 token 中解析 userId 并注入到 @CurrentUser 注解的参数
 */
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessException(ErrorConstants.USER_NOT_EXIST);
        }

        return authentication.getPrincipal(); // 就是 userId
    }
}
