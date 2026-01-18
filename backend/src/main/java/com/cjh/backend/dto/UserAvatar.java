package com.cjh.backend.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class UserAvatar {
    @NotBlank(message = "头像路径不能为空")
    String avatar;
}
