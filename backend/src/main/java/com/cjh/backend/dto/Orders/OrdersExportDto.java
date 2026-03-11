package com.cjh.backend.dto.Orders;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrdersExportDto {
    @ExcelProperty("订单ID")
    private Long id;

    @ExcelProperty("订单编号")
    private String orderNo;

    @ExcelProperty("买家ID")
    private Long buyerId;

    @ExcelProperty("卖家ID")
    private Long sellerId;

    @ExcelProperty("订单总价")
    private BigDecimal totalPrice;

    @ExcelProperty("状态(1待支付 2待发货 3已发货 4已完成 5已取消)")
    private Integer status;

    @ExcelProperty("收货人")
    private String receiverName;

    @ExcelProperty("联系电话")
    private String receiverPhone;

    @ExcelProperty("收货地址")
    private String receiverAddress;

    @ExcelProperty("物流单号")
    private String trackingNo;

    @ExcelProperty("下单时间")
    private LocalDateTime createTime;

    @ExcelProperty("支付时间")
    private LocalDateTime payTime;
}