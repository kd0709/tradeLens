package com.cjh.backend.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 商品分类表
 * @TableName category
 */
@TableName(value ="category")
@Data
public class Category {
    /**
     * 分类ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父分类ID，NULL表示一级分类
     */
    private Long parentId;

    /**
     * 分类层级，从1开始
     */
    private Integer level;

    /**
     * 分类路径，如 1/3/8
     */
    private String path;

    /**
     * 状态：1启用 0禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}