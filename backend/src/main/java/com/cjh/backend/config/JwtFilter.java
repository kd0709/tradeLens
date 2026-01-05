package com.cjh.backend.config;

import com.cjh.backend.utils.JwtUtil;
import com.cjh.backend.utils.TokenBlacklist;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklist tokenBlacklist;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 放行公开接口
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/") || path.equals("/error")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 获取 Token
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":0,\"message\":\"未提供 Token\",\"data\":null}");
            return;
        }

        String token = header.substring(7);

        // 验证 Token 有效性（签名和过期时间）
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":0,\"message\":\"Token 无效或已过期\",\"data\":null}");
            return;
        }

        // 检查 Token 是否在黑名单中（在放行前检查）
        if (tokenBlacklist.isBlacklisted(token)) {
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":0,\"message\":\"Token 已失效，请重新登录\",\"data\":null}");
            return;
        }

        // 验证通过，继续执行
        filterChain.doFilter(request, response);
    }
}