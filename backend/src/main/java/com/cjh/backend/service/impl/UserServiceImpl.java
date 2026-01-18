package com.cjh.backend.service.impl;


import com.cjh.backend.dto.UserPassword;
import com.cjh.backend.dto.UserInfo;
import com.cjh.backend.entity.User;
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
            throw new RuntimeException("用户不存在");
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
            throw new RuntimeException("两次输入的新密码不一致");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证原密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        // 新旧密码不能相同
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new RuntimeException("新密码不能与原密码相同");
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
