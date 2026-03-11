package com.cjh.backend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjh.backend.entity.User;
import com.cjh.backend.service.UserService;
import com.cjh.backend.utils.ExcelUtils;
import com.cjh.backend.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import com.alibaba.excel.EasyExcel;
import com.cjh.backend.dto.User.UserExportDto;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 后台管理系统 - 用户模块 CRUD 控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/system/user")
@RequiredArgsConstructor
public class SystemUserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 1. 分页查询用户列表 (支持多字段模糊搜索)
     */
    @GetMapping("/page")
    public Result<Page<User>> getUserPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        
        log.info("System Admin - 分页查询用户：page={}, size={}, keyword={}", page, size, keyword);
        
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        // 模糊匹配用户名、昵称、手机号或邮箱
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                              .or()
                              .like(User::getNickname, keyword)
                              .or()
                              .like(User::getPhone, keyword)
                              .or()
                              .like(User::getEmail, keyword));
        }
        
        // 默认按创建时间倒序
        wrapper.orderByDesc(User::getCreateTime);
        
        // 直接调用 MyBatis-Plus 的 IService/Mapper 分页方法
        Page<User> result = userService.page(pageParam, wrapper);
        
        // 为了安全，清空返回结果中的密码字段
        result.getRecords().forEach(user -> user.setPassword(null));
        
        return Result.success(result);
    }

    /**
     * 2. 根据 ID 查询单个用户详情
     */
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable("id") Long id) {
        log.info("System Admin - 查询单用户详情：id={}", id);
        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(null); // 抹除密码
            return Result.success(user);
        }
        return Result.fail(404, "用户不存在");
    }

    /**
     * 3. 后台直接新增用户
     */
    @PostMapping
    public Result<String> saveUser(@RequestBody User user) {
        log.info("System Admin - 新增用户：username={}", user.getUsername());
        
        // 校验用户名是否已存在
        long count = userService.count(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
        if (count > 0) {
            return Result.fail(400, "用户名已存在");
        }
        
        // 默认密码处理 (如果没有传密码，默认设置一个如 123456)
        String rawPassword = StringUtils.hasText(user.getPassword()) ? user.getPassword() : "123456";
        user.setPassword(passwordEncoder.encode(rawPassword));
        
        boolean saved = userService.save(user);
        return saved ? Result.success("新增用户成功") : Result.fail("新增用户失败");
    }

    /**
     * 4. 后台修改用户信息 (包含封禁/解封、角色调整等)
     */
    @PutMapping
    public Result<String> updateUser(@RequestBody User user) {
        if (user.getId() == null) {
            return Result.fail(400, "用户ID不能为空");
        }
        log.info("System Admin - 修改用户信息：id={}", user.getId());
        
        // 如果传入了新密码，则进行加密处理
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        boolean updated = userService.updateById(user);
        return updated ? Result.success("修改用户信息成功") : Result.fail("修改失败，用户可能不存在");
    }

    /**
     * 5. 根据 ID 删除用户 (业务上通常建议做逻辑删除，MyBatis-Plus 配置好 @TableLogic 后 removeById 即为逻辑删除)
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable("id") Long id) {
        log.info("System Admin - 删除用户：id={}", id);
        
        // 安全校验：防止管理员删掉自己（假设请求上下文能获取当前登录管理员ID，此处简单处理）
        User user = userService.getById(id);
        if (user != null && user.getRole() != null && user.getRole() == 1) {
            return Result.fail(403, "禁止删除管理员账号");
        }
        
        boolean removed = userService.removeById(id);
        return removed ? Result.success("删除用户成功") : Result.fail("删除失败");
    }

    /**
     * 6. 批量导出用户信息为 Excel (模块3功能)
     */
    @GetMapping("/export")
    public void exportUsers(HttpServletResponse response) {
        List<UserExportDto> exportList = userService.list().stream().map(user -> {
            UserExportDto dto = new UserExportDto();
            BeanUtils.copyProperties(user, dto); // 使用 BeanUtils 快速拷贝同名字段
            return dto;
        }).collect(Collectors.toList());

        // 一行代码完成导出
        ExcelUtils.export(response, "用户信息导出", "用户列表", exportList, UserExportDto.class);
    }
}