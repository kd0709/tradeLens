package com.cjh.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjh.backend.dto.Comment.CommentListDto;
import com.cjh.backend.dto.Comment.CommentPublishDto;
import com.cjh.backend.entity.Comment;
import com.cjh.backend.entity.OrderItem;
import com.cjh.backend.entity.Orders;
import com.cjh.backend.mapper.OrderItemMapper;
import com.cjh.backend.mapper.OrdersMapper;
import com.cjh.backend.service.CommentService;
import com.cjh.backend.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
* @author 45209
* @description 针对表【comment(商品评价表)】的数据库操作Service实现
* @createDate 2026-01-29 18:58:49
*/
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
    implements CommentService{

    private final CommentMapper commentMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrdersMapper ordersMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishComment(Long userId, CommentPublishDto dto) {
        // 1. 校验订单
        Orders order = ordersMapper.selectById(dto.getOrderId());
        if (order == null || !order.getBuyerId().equals(userId)) {
            throw new RuntimeException("订单不存在或无权限评价");
        }
        if (order.getStatus() != 4) {
            throw new RuntimeException("订单未完成，无法评价");
        }

        // 2. 检查已评价
        Long existId = commentMapper.selectIdByOrderId(dto.getOrderId());
        if (existId != null) {
            throw new RuntimeException("该订单已评价，请勿重复提交");
        }

        // 3. 查询订单明细获取 product_id（假设单明细）
        List<OrderItem> items = orderItemMapper.selectByOrderId(dto.getOrderId());
        if (items.isEmpty()) {
            throw new RuntimeException("订单明细不存在");
        }
        Long productId = items.get(0).getProductId();  // 取第一个明细的 product_id

        // 4. 构建评价实体
        Comment comment = new Comment();
        BeanUtils.copyProperties(dto, comment);
        comment.setUserId(userId);
        comment.setProductId(productId);
        comment.setCreateTime(LocalDateTime.now());

        int rows = commentMapper.insert(comment);
        if (rows == 0) {
            throw new IllegalStateException("发表评价失败");
        }
    }

    @Override
    public List<CommentListDto> listCommentsByProductId(Long productId) {
        return commentMapper.selectCommentsByProductId(productId);
    }

}




