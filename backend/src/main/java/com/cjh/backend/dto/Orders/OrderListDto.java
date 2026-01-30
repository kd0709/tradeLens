package com.cjh.backend.dto.Orders;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderListDto {

    private Long id;

    private String orderNo;

    private BigDecimal totalPrice;

    private Integer status;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private String trackingNo;

    private LocalDateTime createTime;

    private LocalDateTime payTime;

    private LocalDateTime deliveryTime;

    private LocalDateTime finishTime;
}