package com.cjh.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjh.backend.dto.User.SellerInfoDto;
import com.cjh.backend.dto.User.UserInfoDto;
import com.cjh.backend.dto.User.UserUpdateDto;
import com.cjh.backend.entity.User;
import com.cjh.backend.service.UserService;
import com.cjh.backend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserInfoDto getUserInfo(Long userId) {
        return userMapper.getUserInfoById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoDto updateUserInfo(Long userId, UserUpdateDto req) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() != 1 || user.getIsDeleted() == 1) {
            throw new IllegalArgumentException("用户不存在或状态异常");
        }
        int rows = userMapper.updateUserProfile(
                userId,
                req.getNickname(),
                req.getPhone(),
                req.getEmail(),
                req.getAvatar()
        );
        if (rows == 0) {
            throw new IllegalArgumentException("更新失败，可能无变更或用户状态异常");
        }
        return userMapper.getUserInfoById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        String currentEncrypted = userMapper.getPasswordById(userId);
        if (currentEncrypted == null) {
            throw new IllegalArgumentException("用户不存在或状态异常");
        }

        if (!passwordEncoder.matches(oldPassword, currentEncrypted)) {
            throw new IllegalArgumentException("旧密码错误");
        }

        String newEncrypted = passwordEncoder.encode(newPassword);

        int rows = userMapper.updatePassword(userId, newEncrypted);
        if (rows == 0) {
            throw new IllegalArgumentException("密码更新失败");
        }
    }

    @Override
    public SellerInfoDto getSellerInfo(Long sellerId) {
        return userMapper.getSellerInfoById(sellerId);
    }
}