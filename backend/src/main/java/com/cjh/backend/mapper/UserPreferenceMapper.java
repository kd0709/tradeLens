package com.cjh.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjh.backend.entity.UserPreference;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserPreferenceMapper extends BaseMapper<UserPreference> {
    
    @Insert("INSERT INTO tradelens.user_preference (user_id, category_id, score) " +
            "VALUES (#{userId}, #{categoryId}, #{score}) " +
            "ON DUPLICATE KEY UPDATE score = score + #{score}")
    void addPreferenceScore(@Param("userId") Long userId, @Param("categoryId") Long categoryId, @Param("score") Integer score);
}