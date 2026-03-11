package com.cjh.backend.dto.User;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserExportDto {
    
    @ExcelProperty("用户ID")
    private Long id;

    @ExcelProperty("用户名")
    private String username;

    @ExcelProperty("用户昵称")
    private String nickname;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("账号状态(1正常 0封禁)")
    private Integer status;

    @ExcelProperty("角色(0普通用户 1管理员)")
    private Integer role;

    @ExcelProperty("注册时间")
    private LocalDateTime createTime;
}