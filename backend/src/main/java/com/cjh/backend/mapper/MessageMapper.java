package com.cjh.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjh.backend.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}