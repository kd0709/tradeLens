package com.cjh.backend.service.impl;


import com.cjh.backend.dto.UserPassword;
import com.cjh.backend.dto.UserInfo;
import com.cjh.backend.entity.User;
import com.cjh.backend.exception.BusinessException;
import com.cjh.backend.exception.ErrorConstants;
import com.cjh.backend.mapper.UserMapper;
import com.cjh.backend.service.UserService;
import com.cjh.backend.utils.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    @Override
    public void updateUserInfo(Long userId, UserInfo request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorConstants.USER_NOT_EXIST);
        }

        // 只更新非空字段
        if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
            user.setNickname(request.getNickname().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        userMapper.updateById(user);
    }

    @Override
    public void updatePassword(Long userId, UserPassword request) {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorConstants.PASSWORD_NOT_MATCH);
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorConstants.USER_NOT_EXIST);
        }

        // 验证原密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorConstants.PASSWORD_OLD_ERROR);
        }

        // 新旧密码不能相同
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BusinessException(ErrorConstants.PASSWORD_SAME_AS_OLD);
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public String updateAvatar(Long userId, MultipartFile updateAvatar) {
        return "";
    }
}
