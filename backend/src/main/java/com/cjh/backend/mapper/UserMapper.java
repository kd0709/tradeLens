package com.cjh.backend.mapper;

import com.cjh.backend.dto.SellerInfoDto;
import com.cjh.backend.dto.UserInfoDto;
import com.cjh.backend.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
* @author 45209
* @description 针对表【user(用户表)】的数据库操作Mapper
* @createDate 2026-01-29 18:58:49
* @Entity com.cjh.backend.entity.User
*/

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户ID查询基本信息（正常状态的用户）
     * 简单查询，使用注解写
     */
    @Select("""
        SELECT 
            id, username, nickname, avatar, phone, email, role, create_time
        FROM tradelens.user
        WHERE id = #{userId}
          AND status = 1
          AND is_deleted = 0
        LIMIT 1
    """)
    UserInfoDto getUserInfoById(@Param("userId") Long userId);

    @Select("select * from tradelens.user where username=#{username}")
    User selectByUsername(@Param("username") String username);

    @Select("SELECT COUNT(*) FROM tradelens.user WHERE username = #{username}")
    int countByUsername(@Param("username") String username);

    int updateUserProfile(
            @Param("userId") Long userId,
            @Param("nickname") String nickname,
            @Param("phone") String phone,
            @Param("email") String email,
            @Param("avatar") String avatar
    );


    @Select("SELECT password FROM tradelens.user WHERE id = #{userId} AND status = 1 AND is_deleted = 0 LIMIT 1")
    String getPasswordById(@Param("userId") Long userId);

    // 更新密码（XML 实现）
    int updatePassword(
            @Param("userId") Long userId,
            @Param("newPassword") String newPassword
    );


    /**
     * 查询卖家公开信息（简单字段 + 商品发布数量统计）
     */
    SellerInfoDto getSellerInfoById(@Param("sellerId") Long sellerId);
}
