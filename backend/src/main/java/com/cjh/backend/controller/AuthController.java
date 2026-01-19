package com.cjh.backend.controller;


import com.cjh.backend.dto.LoginRequest;
import com.cjh.backend.dto.RegisterRequest;
import com.cjh.backend.dto.UserInfo;
import com.cjh.backend.service.AuthService;
import com.cjh.backend.utils.Result;
import com.cjh.backend.utils.TokenBlacklist;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenBlacklist tokenBlacklist;


    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest);
        return Result.success("成功注册");
    }

    @PostMapping("/login")
    public Result<UserInfo> login(@Valid @RequestBody LoginRequest loginRequest) {
        UserInfo userInfo = authService.login(loginRequest);
        return Result.success(userInfo,"登陆成功");
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return Result.fail("未登录或 token 无效");
        }
        Object credentials = auth.getCredentials();
        if (credentials instanceof String token) {
            tokenBlacklist.addToBlacklist(token);
        } else {
            return Result.fail("无法获取 token");
        }
        return Result.success("成功登出");
    }
}
