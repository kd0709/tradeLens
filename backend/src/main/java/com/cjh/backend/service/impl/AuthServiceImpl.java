package com.cjh.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjh.backend.dto.Auth.LoginRequest;
import com.cjh.backend.dto.Auth.RegisterRequest;
import com.cjh.backend.dto.User.UserInfoDto;
import com.cjh.backend.entity.User;
import com.cjh.backend.exception.BusinessException;
import com.cjh.backend.exception.ErrorConstants;
import com.cjh.backend.service.AuthService;
import com.cjh.backend.mapper.UserMapper;
import com.cjh.backend.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
* @author 45209
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2026-01-04 17:15:32
*/
@Service
@RequiredArgsConstructor
public class AuthServiceImpl extends ServiceImpl<UserMapper, User>
    implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    @Override
    public UserInfoDto login(LoginRequest loginRequest) {
        User user = userMapper.selectByUsername(loginRequest.getUsername());

        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorConstants.USERNAME_PASSWORD_ERROR);
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorConstants.USER_FROZEN);
        }

        // 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.setUsername(user.getUsername());
        userInfoDto.setNickname(user.getNickname());
        userInfoDto.setPhone(user.getPhone());
        userInfoDto.setEmail(user.getEmail());
        userInfoDto.setAvatar(user.getAvatar());
        userInfoDto.setToken(token);

        return userInfoDto;


    }

    @Override
    public void register(RegisterRequest registerRequest) {
        if (userMapper.countByUsername(registerRequest.getUsername()) > 0) {
            throw new BusinessException(ErrorConstants.USERNAME_EXIST);
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setNickname("邪恶水蜜桃");

        userMapper.insert(user);
    }
}




