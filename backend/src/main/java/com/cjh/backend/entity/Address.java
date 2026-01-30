package com.cjh.backend.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;
import org.springframework.cglib.core.Local;

/**
 * 收货地址表
 * @TableName address
 */
@TableName(value ="address")
@Data
public class Address {
    /**
     * 地址ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区
     */
    private String district;

    /**
     * 详细地址
     */
    private String detailAddress;

    /**
     * 是否默认：1是 0否
     */
    private Integer isDefault;

    /**
     * 逻辑删除
     */
    private Integer isDeleted;

    /**
     * 
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}