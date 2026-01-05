package com.cjh.backend.mapper;

import com.cjh.backend.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
* @author 45209
* @description 针对表【user(用户表)】的数据库操作Mapper
* @createDate 2026-01-04 17:15:32
* @Entity com.cjh.backend.entity.User
*/
public interface UserMapper extends BaseMapper<User> {

    @Select("select * from idle_trade.user where username=#{username}")
    User selectByUsername(String username);

    @Select("select count(1) from idle_trade.user where username=#{username}")
    int countByUsername(String username);

}




