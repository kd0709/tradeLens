package com.cjh.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 全局跨域配置
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 允许所有来源（生产环境建议改为具体域名）
        config.addAllowedOriginPattern("*");  // 或 config.addAllowedOrigin("http://localhost:5173");

        // 允许的请求头
        config.addAllowedHeader("*");

        // 允许的 HTTP 方法
        config.addAllowedMethod("*");

        // 是否允许发送 Cookie（如果用 cookie 认证需开启）
        config.setAllowCredentials(true);

        // 预检请求缓存时间（秒）
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}