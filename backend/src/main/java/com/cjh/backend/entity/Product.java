package com.cjh.backend.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 商品表
 * @TableName product
 */
@TableName(value ="product")
@Data
public class Product {
    /**
     * 商品ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发布者ID
     */
    private Long userId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 商品标题
     */
    private String title;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 新旧程度：1全新 2几乎全新 3轻微使用 4明显使用
     */
    private Integer conditionLevel;

    /**
     * 是否可议价：1是 0否
     */
    private Integer negotiable;

    /**
     * 状态：1待审核 2上架 3下架 4已售
     */
    private Integer productStatus;

    /**
     * 逻辑删除：0否 1是
     */
    private Integer isDeleted;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}