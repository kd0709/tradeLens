package com.cjh.backend.service;

import com.cjh.backend.dto.UserPassword;
import com.cjh.backend.dto.UserInfo;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    public void updateUserInfo(Long id, UserInfo userInfo);

    public void updatePassword(Long id, UserPassword userPassword);

    public String updateAvatar(Long id, MultipartFile updateAvatar);

}
