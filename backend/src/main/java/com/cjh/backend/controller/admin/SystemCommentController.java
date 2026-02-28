package com.cjh.backend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjh.backend.entity.Comment;
import com.cjh.backend.service.CommentService;
import com.cjh.backend.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理系统 - 评价模块 CRUD 控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/system/comment")
@RequiredArgsConstructor
public class SystemCommentController {

    private final CommentService commentService;

    /**
     * 1. 分页查询评价列表 (支持评价内容关键词搜索)
     */
    @GetMapping("/page")
    public Result<Page<Comment>> getCommentPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        
        log.info("System Admin - 分页查询评价：page={}, size={}, keyword={}", page, size, keyword);
        Page<Comment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Comment::getContent, keyword);
        }
        
        wrapper.orderByDesc(Comment::getCreateTime);
        return Result.success(commentService.page(pageParam, wrapper));
    }

    /**
     * 2. 查询单条评价详情
     */
    @GetMapping("/{id}")
    public Result<Comment> getCommentById(@PathVariable("id") Long id) {
        log.info("System Admin - 查询评价详情：id={}", id);
        Comment comment = commentService.getById(id);
        if (comment != null) {
            return Result.success(comment);
        }
        return Result.fail(404, "评价不存在");
    }

    /**
     * 3. 修改评价 (如管理员屏蔽违规内容)
     */
    @PutMapping
    public Result<String> updateComment(@RequestBody Comment comment) {
        if (comment.getId() == null) {
            return Result.fail(400, "评价ID不能为空");
        }
        log.info("System Admin - 修改评价内容：id={}", comment.getId());
        boolean updated = commentService.updateById(comment);
        return updated ? Result.success("修改评价成功") : Result.fail("修改失败");
    }

    /**
     * 4. 删除评价 (管理员清理垃圾评论)
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteComment(@PathVariable("id") Long id) {
        log.info("System Admin - 删除违规评价：id={}", id);
        boolean removed = commentService.removeById(id);
        return removed ? Result.success("删除评价成功") : Result.fail("删除失败");
    }
}