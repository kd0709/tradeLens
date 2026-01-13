package com.cjh.backend.service;

import com.cjh.backend.dto.UpdateAvatar;
import com.cjh.backend.dto.UpdatePassword;
import com.cjh.backend.dto.UserInfo;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    public void updateUserInfo(Long id,UserInfo userInfo);

    public void updatePassword(Long id, UpdatePassword updatePassword);

    public String updateAvatar(Long id, MultipartFile updateAvatar);

}
