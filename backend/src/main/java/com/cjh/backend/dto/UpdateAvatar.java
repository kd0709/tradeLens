package com.cjh.backend.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class UpdateAvatar {
    @NotBlank(message = "头像路径不能为空")
    String avatar;
}
