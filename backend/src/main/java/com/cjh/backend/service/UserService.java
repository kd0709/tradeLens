package com.cjh.backend.service;

import com.cjh.backend.dto.User.SellerInfoDto;
import com.cjh.backend.dto.User.UserInfoDto;
import com.cjh.backend.dto.User.UserUpdateDto;
import com.cjh.backend.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 45209
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2026-01-29 18:58:49
*/
public interface UserService extends IService<User> {

    UserInfoDto getUserInfo(Long userId);

    UserInfoDto updateUserInfo(Long userId, UserUpdateDto req);

    void updatePassword(Long userId, String oldPassword, String newPassword);

    SellerInfoDto getSellerInfo(Long sellerId);
}
