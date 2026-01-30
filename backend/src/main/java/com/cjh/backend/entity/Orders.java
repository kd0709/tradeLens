package com.cjh.backend.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 订单表
 * @TableName orders
 */
@TableName(value ="orders")
@Data
public class Orders {
    /**
     * 订单ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 买家ID
     */
    private Long buyerId;

    /**
     * 卖家ID
     */
    private Long sellerId;

    /**
     * 订单总价
     */
    private BigDecimal totalPrice;

    /**
     * 状态：1待支付 2待发货 3已发货 4已完成 5已取消
     */
    private Integer status;

    /**
     * 收货人快照
     */
    private String receiverName;

    /**
     * 电话快照
     */
    private String receiverPhone;

    /**
     * 完整地址快照
     */
    private String receiverAddress;

    /**
     * 物流单号
     */
    private String trackingNo;

    /**
     * 
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 发货时间
     */
    private LocalDateTime deliveryTime;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;
}