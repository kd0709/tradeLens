package com.cjh.backend.controller;

import com.alipay.api.internal.util.AlipaySignature;
import com.cjh.backend.config.AlipayConfig;
import com.cjh.backend.entity.Orders;
import com.cjh.backend.mapper.OrdersMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/alipay")
@RequiredArgsConstructor
public class AlipayController {
    private final AlipayConfig alipayConfig;
    private final OrdersMapper ordersMapper;

    @PostMapping("/notify")
    public String handleNotify(HttpServletRequest request) throws Exception {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            params.put(name, request.getParameter(name));
        }

        // 验证签名是否来自支付宝
        boolean verifyResult = AlipaySignature.rsaCheckV1(params, alipayConfig.getAlipayPublicKey(), 
                alipayConfig.getCharset(), alipayConfig.getSignType());

        if (verifyResult && "TRADE_SUCCESS".equals(params.get("trade_status"))) {
            String orderNo = params.get("out_trade_no");
            Orders order = ordersMapper.selectByOrderNo(orderNo);
            if (order != null && order.getStatus() == 1) {
                order.setStatus(2); // 支付成功，状态转为待发货
                order.setPayTime(LocalDateTime.now());
                ordersMapper.updateById(order);
            }
            return "success"; // 告知支付宝已处理
        }
        return "fail";
    }
}