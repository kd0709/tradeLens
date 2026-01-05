package com.cjh.backend.controller;


import com.cjh.backend.dto.LoginRequest;
import com.cjh.backend.dto.RegisterRequest;
import com.cjh.backend.dto.UserInfo;
import com.cjh.backend.service.UserService;
import com.cjh.backend.utils.JwtUtil;
import com.cjh.backend.utils.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;


    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest registerRequest) {
        userService.register(registerRequest);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<UserInfo> login(@Valid @RequestBody LoginRequest loginRequest) {
        UserInfo userInfo = userService.login(loginRequest);
        return Result.success(userInfo);
    }



}
