package com.cjh.backend.dto;


import lombok.Data;

@Data
public class UserInfo {
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
    private String token;
}
