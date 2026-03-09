package com.cjh.backend.controller;

import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.cjh.backend.config.AlipayConfig;
import com.cjh.backend.entity.Orders;
import com.cjh.backend.mapper.OrdersMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/alipay")
@RequiredArgsConstructor
public class AlipayController {

    private final AlipayConfig alipayConfig;
    private final OrdersMapper ordersMapper;
    private final AlipayClient alipayClient;

    @PostMapping("/notify")
    public String handleNotify(HttpServletRequest request) {
        log.info("📢 收到支付宝异步回调通知...");
        try {
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();

            for (String name : requestParams.keySet()) {
                params.put(name, request.getParameter(name));
            }

            boolean verifyResult = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(),
                    alipayConfig.getSignType()
            );

            if (!verifyResult) {
                log.error("❌ 支付宝回调签名验证失败！");
                return "fail";
            }

            String tradeStatus = params.get("trade_status");
            if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
                return "success";
            }

            if (!alipayConfig.getAppId().equals(params.get("app_id"))) {
                log.error("❌ 支付宝回调appId不匹配！");
                return "fail";
            }

            String orderNo = params.get("out_trade_no");
            String totalAmount = params.get("total_amount");
            Orders order = ordersMapper.selectByOrderNo(orderNo);

            if (order == null) {
                log.error("❌ 支付宝回调未找到对应订单：{}", orderNo);
                return "fail";
            }

            BigDecimal payAmount = new BigDecimal(totalAmount);
            if (order.getTotalPrice().compareTo(payAmount) != 0) {
                log.error("❌ 支付宝回调金额校验失败！");
                return "fail";
            }

            // 幂等与并发处理
            if (order.getStatus() != 1) {
                log.info("⚠️ 订单 {} 状态已被处理过，当前状态: {}", orderNo, order.getStatus());

                // 解决并发漏洞：如果订单超时被定时任务取消了，但用户实际支付成功了，必须发起退款
                if (order.getStatus() == 5) {
                    log.warn("⚠️ 订单 {} 已超时取消但用户支付成功，触发自动退款", orderNo);
                    autoRefund(orderNo, totalAmount);
                }
                return "success";
            }

            order.setStatus(2); // 2 =已支付待发货
            order.setPayTime(LocalDateTime.now());
            order.setTrackingNo(params.get("trade_no"));
            ordersMapper.updateById(order);

            log.info("🎉 订单 {} 支付处理完毕！", orderNo);
            return "success";

        } catch (Exception e) {
            log.error("❌ 处理支付宝回调发生系统异常", e);
            return "fail";
        }
    }

    private void autoRefund(String orderNo, String amount) {
        try {
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", orderNo);
            bizContent.put("refund_amount", amount);
            bizContent.put("refund_reason", "订单超时关闭自动退款");
            request.setBizContent(bizContent.toString());
            alipayClient.execute(request);
            log.info("✅ 订单 {} 自动退款调用成功", orderNo);
        } catch (AlipayApiException e) {
            log.error("❌ 订单 {} 自动退款失败，需人工介入", orderNo, e);
        }
    }
}