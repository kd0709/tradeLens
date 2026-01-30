package com.cjh.backend.controller;


import com.cjh.backend.dto.Comment.CommentListDto;
import com.cjh.backend.dto.Comment.CommentPublishDto;
import com.cjh.backend.service.CommentService;
import com.cjh.backend.utils.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cjh.backend.common.CurrentUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/publish")
    public Result<String> publishComment(
            @CurrentUser Long userId,
            @Valid @RequestBody CommentPublishDto dto) {
        log.info("用户 {} 发表评价，订单ID: {}, 评分: {}", userId, dto.getOrderId(), dto.getScore());
        try {
            commentService.publishComment(userId, dto);
            return Result.success("评价发表成功");
        } catch (Exception e) {
            log.error("用户 {} 发表评价失败", userId, e);
            return Result.fail("评价失败：" + e.getMessage());
        }
    }

    @GetMapping("/list/{productId}")
    public Result<List<CommentListDto>> listCommentsByProductId(@PathVariable Long productId) {
        log.info("查询商品 {} 的评价列表", productId);
        try {
            List<CommentListDto> list = commentService.listCommentsByProductId(productId);
            log.info("商品 {} 评价列表查询成功，共 {} 条", productId, list.size());
            return Result.success(list);
        } catch (Exception e) {
            log.error("查询商品 {} 评价列表异常", productId, e);
            return Result.fail("获取评价列表失败");
        }
    }
}