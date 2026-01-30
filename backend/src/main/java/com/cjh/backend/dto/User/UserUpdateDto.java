// com.cjh.backend.dto.user.UserUpdateReq.java
package com.cjh.backend.dto.User;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UserUpdateDto {

    @NotBlank(message = "昵称不能为空")
    @Length(min = 2, max = 20, message = "昵称长度应在2-20个字符之间")
    private String nickname;

    private String phone;

    private String email;

    // avatar 可以为空（不修改时不传或传空字符串）
    private String avatar;
}