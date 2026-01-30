package com.cjh.backend.dto.Auth;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 6,max = 20,message = "6~20")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6,max = 20,message = "6~32")
    private String password;
}
