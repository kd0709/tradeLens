package com.cjh.backend.service;

import com.cjh.backend.dto.Comment.CommentListDto;
import com.cjh.backend.dto.Comment.CommentPublishDto;
import com.cjh.backend.entity.Comment;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 45209
* @description 针对表【comment(商品评价表)】的数据库操作Service
* @createDate 2026-01-29 18:58:49
*/
public interface CommentService extends IService<Comment> {

    /**
     * 发表评价（一个订单只能评价一次）
     */
    void publishComment(Long userId, CommentPublishDto dto);

    /**
     * 获取商品评价列表
     */
    List<CommentListDto> listCommentsByProductId(Long productId);

}
