package com.cjh.backend.controller;


import com.cjh.backend.common.CurrentUser;
import com.cjh.backend.dto.UpdatePassword;
import com.cjh.backend.dto.UserInfo;
import com.cjh.backend.service.UserService;
import com.cjh.backend.utils.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @PutMapping("/info")
    public Result<String> updateInfo(@CurrentUser Long userId ,
                                     @Valid @RequestBody UserInfo request) {
        System.out.println("=== 更新用户信息 ===");
        System.out.println("userId: " + userId);
        System.out.println("request: " + request);
        userService.updateUserInfo(userId, request);
        return Result.success("个人信息修改成功");
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<String> changePassword(@CurrentUser Long userId ,
                                         @Valid @RequestBody UpdatePassword request) {
        userService.updatePassword(userId, request);
        return Result.success("密码修改成功，请重新登录");
    }

    /**
     * 修改头像（文件上传）
     */
    @PutMapping("/avatar")
    public Result<String> updateAvatar(@CurrentUser Long userId ,
                                       @Valid @RequestParam("avatar") MultipartFile avatar) {
        String avatarUrl = userService.updateAvatar(userId, avatar);
        return Result.success("头像修改成功");
    }

}
