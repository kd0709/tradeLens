package com.cjh.backend.service;

import com.cjh.backend.dto.LoginRequest;
import com.cjh.backend.dto.RegisterRequest;
import com.cjh.backend.dto.UserInfo;
import com.cjh.backend.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 45209
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2026-01-04 17:15:32
*/
public interface AuthService extends IService<User> {

    void register(RegisterRequest registerRequest);

    UserInfo login(LoginRequest loginRequest);
}
