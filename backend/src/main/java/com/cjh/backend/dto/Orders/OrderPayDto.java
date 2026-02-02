package com.cjh.backend.dto.Orders;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderPayDto {

    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @NotBlank(message = "支付方式不能为空")
    private String payType;  // alipay, wechat, etc.
}
