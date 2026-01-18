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

        String path = request.getRequestURI();

        if (path.startsWith("/api/auth/") || path.equals("/error")) {
            System.out.println("放行公开接口");
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        System.out.println("Authorization header: " + header);
        if (header == null || !header.startsWith("Bearer ")) {
            System.out.println("401: 未提供 Token");
            writeErrorResponse(response, 401, "未提供 Token");
            return;
        }

        String token = header.substring(7);
        System.out.println("Token: " + token.substring(0, Math.min(50, token.length())) + "...");

        if (!jwtUtil.validateToken(token)) {
            System.out.println("401: Token 验证失败");
            writeErrorResponse(response, 401, "Token 无效或已过期");
            return;
        }

        if ( jwtUtil.isTokenExpired(token)) {
            System.out.println("401: Token 已过期");
            writeErrorResponse(response, 401, "Token 无效或已过期");
            return;
        }

        if (tokenBlacklist.isBlacklisted(token)) {
            System.out.println("401: Token 在黑名单中");
            writeErrorResponse(response, 401, "Token 已失效，请重新登录");
            return;
        }

        Long userId = jwtUtil.getUserId(token);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,          // principal
                        token,
                        Collections.emptyList()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

}