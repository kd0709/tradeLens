package com.cjh.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 商品评价表
 * @TableName comment
 */
@TableName(value ="comment")
@Data
public class Comment {
    /**
     * 评价ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 评价用户ID
     */
    private Long userId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 评分：1-5
     */
    private Integer score;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}