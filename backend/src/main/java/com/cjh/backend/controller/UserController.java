package com.cjh.backend.controller;


import com.cjh.backend.common.CurrentUser;
import com.cjh.backend.dto.PasswordUpdateDto;
import com.cjh.backend.dto.SellerInfoDto;
import com.cjh.backend.dto.UserInfoDto;
import com.cjh.backend.dto.UserUpdateDto;
import com.cjh.backend.service.UserService;
import com.cjh.backend.utils.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    public Result<UserInfoDto> getCurrentUserInfo(@CurrentUser Long userId) {
        log.info("用户 {} 查询个人信息", userId);
        try {
            UserInfoDto info = userService.getUserInfo(userId);
            if (info == null) {
                log.warn("用户 {} 信息不存在", userId);
                return Result.fail(404, "用户信息不存在");
            }
            log.info("用户 {} 个人信息查询成功：nickname={}", userId, info.getNickname());
            return Result.success(info);
        } catch (Exception e) {
            log.error("用户 {} 查询个人信息异常", userId, e);
            return Result.fail("获取个人信息失败");
        }
    }

    /**
     * 修改用户信息
     */
    @PutMapping("/info")
    public Result<UserInfoDto> updateUserInfo(
            @RequestBody @Valid UserUpdateDto req,
            @CurrentUser Long userId) {
        log.info("用户 {} 尝试修改个人信息：nickname={}, avatar={}",
                userId, req.getNickname(), req.getAvatar());
        try {
            UserInfoDto updated = userService.updateUserInfo(userId, req);
            log.info("用户 {} 个人信息修改成功：nickname={}", userId, updated.getNickname());
            return Result.success(updated, "修改成功");
        } catch (IllegalArgumentException e) {
            log.warn("用户 {} 修改个人信息失败：{}", userId, e.getMessage());
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("用户 {} 修改个人信息异常", userId, e);
            return Result.fail("修改失败，请稍后重试");
        }
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<String> updatePassword(
            @RequestBody @Valid PasswordUpdateDto req,
            @CurrentUser Long userId) {
        log.info("用户 {} 尝试修改密码", userId);
        try {
            userService.updatePassword(userId, req.getOldPassword(), req.getNewPassword());
            log.info("用户 {} 密码修改成功", userId);
            return Result.success("密码修改成功");
        } catch (IllegalArgumentException e) {
            log.warn("用户 {} 修改密码失败：{}", userId, e.getMessage());
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("用户 {} 修改密码异常", userId, e);
            return Result.fail("修改密码失败，请稍后重试");
        }
    }


    /**
     * 获取卖家信息
     */
    @GetMapping("/seller/{id}")
    public Result<SellerInfoDto> getSellerInfo(@PathVariable("id") Long sellerId) {
        log.info("查询卖家公开信息：sellerId = {}", sellerId);
        try {
            SellerInfoDto sellerInfo = userService.getSellerInfo(sellerId);
            if (sellerInfo == null) {
                log.warn("卖家信息不存在：sellerId = {}", sellerId);
                return Result.fail(404, "卖家不存在");
            }

            log.info("卖家信息查询成功：sellerId = {}, nickname = {}",
                    sellerId, sellerInfo.getNickname());
            return Result.success(sellerInfo);
        } catch (Exception e) {
            log.error("查询卖家信息异常：sellerId = {}", sellerId, e);
            return Result.fail("获取卖家信息失败");
        }
    }



}
