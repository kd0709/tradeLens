package com.cjh.backend.controller;

import com.alipay.api.internal.util.AlipaySignature;
import com.cjh.backend.config.AlipayConfig;
import com.cjh.backend.entity.Orders;
import com.cjh.backend.entity.OrderItem;
import com.cjh.backend.mapper.OrdersMapper;
import com.cjh.backend.mapper.OrderItemMapper;
import com.cjh.backend.mapper.ProductMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alipay")
@RequiredArgsConstructor
public class AlipayController {

    private final AlipayConfig alipayConfig;
    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    
    private final ProductMapper productMapper;


    @PostMapping("/notify")
    public String handleNotify(HttpServletRequest request) throws Exception {

        // 1. 获取支付宝回调参数
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();

        for (String name : requestParams.keySet()) {
            params.put(name, request.getParameter(name));
        }

        // 2. 验证签名
        boolean verifyResult = AlipaySignature.rsaCheckV1(
                params,
                alipayConfig.getAlipayPublicKey(),
                alipayConfig.getCharset(),
                alipayConfig.getSignType()
        );

        if (!verifyResult) {
            return "fail";
        }

        // 3. 判断交易状态
        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus)) {
            return "fail";
        }

        // 4. 校验 appId
        if (!alipayConfig.getAppId().equals(params.get("app_id"))) {
            return "fail";
        }

        // 5. 获取订单号和金额
        String orderNo = params.get("out_trade_no");
        String totalAmount = params.get("total_amount");

        Orders order = ordersMapper.selectByOrderNo(orderNo);

        if (order == null) {
            return "fail";
        }

        // 6. 校验金额
//        if (!order.getTotalAmount().toString().equals(totalAmount)) {
//            return "fail";
//        }

        // 7. 幂等处理（已支付直接返回 success）
        if (order.getStatus() != 1) {
            return "success";
        }

        // 8. 更新订单状态（库存已在下单时预扣减）
        order.setStatus(2); // 2 =已支付待发货
        order.setPayTime(LocalDateTime.now());
                
        ordersMapper.updateById(order);

        return "success";
    }
}