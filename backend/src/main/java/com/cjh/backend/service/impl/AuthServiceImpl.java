package com.cjh.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjh.backend.dto.LoginRequest;
import com.cjh.backend.dto.RegisterRequest;
import com.cjh.backend.dto.UserInfo;
import com.cjh.backend.entity.User;
import com.cjh.backend.service.AuthService;
import com.cjh.backend.mapper.UserMapper;
import com.cjh.backend.utils.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
* @author 45209
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2026-01-04 17:15:32
*/
@Service
@AllArgsConstructor
public class AuthServiceImpl extends ServiceImpl<UserMapper, User>
    implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    @Override
    public UserInfo login(LoginRequest loginRequest) {
        User user = userMapper.selectByUsername(loginRequest.getUsername());

        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已被封禁");
        }

        // 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setToken(token);

        return userInfo;


    }

    @Override
    public void register(RegisterRequest registerRequest) {
        if (userMapper.countByUsername(registerRequest.getUsername()) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setNickname("邪恶水蜜桃");

        userMapper.insert(user);
    }
}




