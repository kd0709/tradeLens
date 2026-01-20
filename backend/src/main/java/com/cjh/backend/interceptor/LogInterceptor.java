package com.cjh.backend.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LogInterceptor implements HandlerInterceptor {

    // 存放请求开始时间
    private static final String START_TIME = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        // 请求开始时间
        request.setAttribute(START_TIME, System.currentTimeMillis());
        return true; // 继续处理
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        long startTime = (Long) request.getAttribute(START_TIME);
        long duration = System.currentTimeMillis() - startTime;

        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();

        System.out.println(String.format(
                "[请求完成] %s %s -> 状态码: %d, 耗时: %dms",
                method, uri, status, duration
        ));

        if (ex != null) {
            System.out.println("[异常] " + ex.getMessage());
        }
    }
}
