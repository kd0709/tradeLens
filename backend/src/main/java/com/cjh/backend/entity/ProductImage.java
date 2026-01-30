package com.cjh.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 商品图片表
 * @TableName product_image
 */
@TableName(value ="product_image")
@Data
public class ProductImage {
    /**
     * 图片ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 是否主图：1是 0否
     */
    private Integer isMain;

    /**
     * 排序值，越小越靠前
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}