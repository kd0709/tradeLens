package com.cjh.backend.dto.Auth;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordUpdateDto {


    @NotBlank(message = "旧密码不能为空")
    String oldPassword;
    @NotBlank(message = "新密码不能为空")
    String newPassword;
    @NotBlank(message = "确认密码不能为空")
    String confirmPassword;

}
