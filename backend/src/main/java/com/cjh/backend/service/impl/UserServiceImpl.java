package com.cjh.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjh.backend.entity.User;
import com.cjh.backend.service.UserService;
import com.cjh.backend.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author 45209
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2026-01-04 17:15:32
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




