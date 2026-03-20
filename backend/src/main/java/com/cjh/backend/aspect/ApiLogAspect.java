package com.cjh.backend.aspect;

import com.cjh.backend.annotation.ApiLog;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class ApiLogAspect {

    @Around("@annotation(apiLog)")
    public Object recordApiLog(ProceedingJoinPoint joinPoint, ApiLog apiLog) throws Throwable {
        long startTime = System.currentTimeMillis();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        String method = request != null ? request.getMethod() : "UNKNOWN";
        String uri = request != null ? request.getRequestURI() : "UNKNOWN";
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            log.info("[AOP API日志] {} | {} {} | 执行方法: {}.{}() | 耗时: {}ms",
                    apiLog.value(), method, uri, className, methodName, duration);
            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[AOP API异常] {} | {} {} | 执行方法: {}.{}() | 耗时: {}ms | 异常信息: {}",
                    apiLog.value(), method, uri, className, methodName, duration, e.getMessage());
            throw e;
        }
    }
}