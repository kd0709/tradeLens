package com.cjh.backend.controller;


import com.cjh.backend.dto.Auth.LoginRequest;
import com.cjh.backend.dto.Auth.RegisterRequest;
import com.cjh.backend.dto.User.UserInfoDto;
import com.cjh.backend.service.AuthService;
import com.cjh.backend.service.UserService;
import com.cjh.backend.utils.JwtUtil;
import com.cjh.backend.utils.Result;
import com.cjh.backend.utils.TokenBlacklist;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenBlacklist tokenBlacklist;
    private final JwtUtil jwtUtil;


    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("用户 {} 注册", registerRequest.getUsername());
        try {
            authService.register(registerRequest);
            log.info("注册成功");
            return Result.success("注册成功");
        } catch (Exception e) {
            log.info("注册失败", e);
            return Result.fail("注册失败");
        }
    }

    @PostMapping("/login")
    public Result<UserInfoDto> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("用户 {} 登录", loginRequest.getUsername());
        try {
            UserInfoDto userInfoDto = authService.login(loginRequest);
            log.info("登录成功");
            return Result.success(userInfoDto, "登陆成功");
        } catch (Exception e) {
            log.error("用户登陆失败", e);
            return Result.fail("登陆失败");
        }
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.fail("未登录或 token 无效");
        }

        String token = authHeader.substring(7);

        try {
            // 1. 获取这个 Token 本来的过期时间点
            long expirationTime = jwtUtil.extractAllClaims(token).getExpiration().getTime();
            long now = System.currentTimeMillis();

            // 2. 计算距离现在还剩多少秒过期
            long remainSeconds = (expirationTime - now) / 1000;

            // 3. 将它打入黑名单，设置精确的倒计时
            if (remainSeconds > 0) {
                tokenBlacklist.addToBlacklist(token, remainSeconds);
                log.info("用户主动登出，Token已入黑名单，剩余 {} 秒", remainSeconds);
            }
        } catch (Exception e) {
            log.warn("登出时 Token 已过期或非法: {}", e.getMessage());
        }

        // 4. 清理 Spring Security 上下文
        SecurityContextHolder.clearContext();
        return Result.success("成功登出");
    }
}
