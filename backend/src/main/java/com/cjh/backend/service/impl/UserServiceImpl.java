package com.cjh.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjh.backend.dto.User.SellerInfoDto;
import com.cjh.backend.dto.User.UserInfoDto;
import com.cjh.backend.dto.User.UserUpdateDto;
import com.cjh.backend.entity.User;
import com.cjh.backend.service.UserService;
import com.cjh.backend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
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



    /**
     * 变更用户信用分
     * @param userId 用户ID
     * @param score 变动分数 (正数为加分，负数为扣分)
     * @param reason 变动原因 (可用于记录日志)
     */
    @Override
    public void changeCreditScore(Long userId, int score, String reason) {
        User user = this.getById(userId);
        if (user == null) return;

        int newScore = user.getCreditScore() + score;
        // 限制最高 120 分，最低 0 分
        if (newScore > 120) newScore = 120;
        if (newScore < 0) newScore = 0;

        user.setCreditScore(newScore);

         //如果分数低于 30 分，可以触发账号异常状态（例如 status = 0 限制发布）
         if (newScore < 30) {
             user.setStatus(0); // 假设 0 为限制状态
         }

        this.updateById(user);

        log.info("用户 {} 信用分变动: {}, 当前分数: {}, 原因: {}", userId, score, newScore, reason);
    }
}