package com.cjh.backend.service.impl;

import com.cjh.backend.entity.User;
import com.cjh.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final UserService userService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 异步发送商品审核通过邮件
     */
    @Async
    public void sendProductAuditPassEmail(Long userId, String productTitle) {
        try {
            User user = userService.getById(userId);
            // 确保用户存在且已绑定邮箱
            if (user != null && user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(user.getEmail());
                message.setSubject("【闲置物品慧选平台】商品审核通过通知");
                message.setText("亲爱的 " + user.getNickname() + "，\n\n您好！\n\n您发布的商品【" + productTitle + "】已经审核通过，现已成功上架！\n\n感谢您使用本论文涉及的平台进行交易。");
                
                mailSender.send(message);
                log.info("审核通过邮件已成功发送至用户邮箱: {}", user.getEmail());
            } else {
                log.warn("用户 {} 未绑定邮箱或邮箱为空，跳过邮件发送", userId);
            }
        } catch (Exception e) {
            log.error("发送审核邮件通知失败，userId: {}", userId, e);
        }
    }
}