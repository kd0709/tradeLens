package com.cjh.backend.controller;

import com.alipay.api.internal.util.AlipaySignature;
import com.cjh.backend.config.AlipayConfig;
import com.cjh.backend.entity.Orders;
import com.cjh.backend.mapper.OrdersMapper;
import com.cjh.backend.mapper.OrderItemMapper;
import com.cjh.backend.mapper.ProductMapper;
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
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;

    @PostMapping("/notify")
    public String handleNotify(HttpServletRequest request) {
        log.info("📢 收到支付宝异步回调通知...");
        try {
            // 1. 获取支付宝回调参数
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();

            for (String name : requestParams.keySet()) {
                params.put(name, request.getParameter(name));
            }
            log.info("回调参数: {}", params);

            // 2. 验证签名
            boolean verifyResult = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(),
                    alipayConfig.getSignType()
            );

            if (!verifyResult) {
                log.error("❌ 支付宝回调签名验证失败！请检查 alipayPublicKey 是否为支付宝公钥（非应用公钥）");
                return "fail";
            }
            log.info("✅ 签名验证通过");

            // 3. 判断交易状态
            String tradeStatus = params.get("trade_status");
            log.info("交易状态: {}", tradeStatus);
            if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
                return "success"; // 非成功状态也需返回success给支付宝，避免重复通知
            }

            // 4. 校验 appId
            if (!alipayConfig.getAppId().equals(params.get("app_id"))) {
                log.error("❌ 支付宝回调appId不匹配！接收到的appId: {}", params.get("app_id"));
                return "fail";
            }

            // 5. 获取订单号和金额
            String orderNo = params.get("out_trade_no");
            String totalAmount = params.get("total_amount");

            Orders order = ordersMapper.selectByOrderNo(orderNo);

            if (order == null) {
                log.error("❌ 支付宝回调未找到对应订单：{}", orderNo);
                return "fail";
            }

            // 6. 校验金额
            BigDecimal payAmount = new BigDecimal(totalAmount);
            if (order.getTotalPrice().compareTo(payAmount) != 0) {
                log.error("❌ 支付宝回调金额校验失败！系统订单金额：{}， 实际支付金额：{}", order.getTotalPrice(), payAmount);
                return "fail";
            }
            log.info("✅ 金额校验通过");

            // 7. 幂等处理（已支付直接返回 success）
            if (order.getStatus() != 1) {
                log.info("⚠️ 订单 {} 状态已被处理过，当前状态: {}", orderNo, order.getStatus());
                return "success";
            }

            // 8. 更新订单状态（库存已在下单时预扣减）
            order.setStatus(2); // 2 =已支付待发货
            order.setPayTime(LocalDateTime.now());
            // 记录支付宝交易流水号，方便后续退款对账
            order.setTrackingNo(params.get("trade_no"));

            ordersMapper.updateById(order);
            log.info("🎉 订单 {} 支付处理完毕，状态成功更新为已支付！", orderNo);

            return "success";

        } catch (Exception e) {
            log.error("❌ 处理支付宝回调发生系统异常", e);
            return "fail";
        }
    }
}