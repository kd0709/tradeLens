package com.cjh.backend.config;

import com.cjh.backend.utils.JwtUtil;
import com.cjh.backend.utils.TokenBlacklist;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklist tokenBlacklist;

    private void writeErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":0,\"message\":\"" + message + "\",\"data\":null}");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        // 提示：既然已经重构到 MinIO，这里的 /images/ 白名单可能不再需要了，看你业务决定是否删掉
        if (path.startsWith("/api/auth/")
                || path.equals("/error")
                || path.equals("/api/alipay/notify")
                || path.startsWith("/api/ws/")){
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            writeErrorResponse(response, 401, "未提供 Token");
            return;
        }

        String token = header.substring(7);

        // ============================================
        // 核心重构区：先拦截已经被主动废弃的 Token
        // ============================================
        if (tokenBlacklist.contains(token)) {
            writeErrorResponse(response, 401, "Token 已失效，请重新登录");
            return;
        }

        // ============================================
        // 通过安检后，再去验证签名和是否自然过期
        // ============================================
        if (!jwtUtil.validateToken(token)) {
            writeErrorResponse(response, 401, "Token 无效或已过期");
            return;
        }

        Long userId = jwtUtil.getUserId(token);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        Collections.emptyList()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}