package com.cjh.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjh.backend.entity.Comment;
import com.cjh.backend.service.CommentService;
import com.cjh.backend.mapper.CommentMapper;
import org.springframework.stereotype.Service;

/**
* @author 45209
* @description 针对表【comment(商品评价表)】的数据库操作Service实现
* @createDate 2026-01-29 18:58:49
*/
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
    implements CommentService{

}




