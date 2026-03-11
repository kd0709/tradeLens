package com.cjh.backend.websocket;

import com.alibaba.fastjson.JSON;
import com.cjh.backend.entity.Message;
import com.cjh.backend.mapper.MessageMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServerEndpoint("/api/ws/{userId}")
@Component
public class WebSocketServer {

    private static final Map<Long, Session> sessionMap = new ConcurrentHashMap<>();
    
    // 静态注入 Mapper 供 WebSocket 实例使用
    private static MessageMapper messageMapper;

    @Autowired
    public void setMessageMapper(MessageMapper messageMapper) {
        WebSocketServer.messageMapper = messageMapper;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        sessionMap.put(userId, session);
        log.info("用户 {} 建立 WebSocket 连接", userId);
    }

    @OnClose
    public void onClose(@PathParam("userId") Long userId) {
        sessionMap.remove(userId);
        log.info("用户 {} 断开 WebSocket 连接", userId);
    }

    @OnMessage
    public void onMessage(String messageStr, @PathParam("userId") Long userId) {
        try {
            // 前端 JSON 示例: {"receiverId":2, "content":"你好"}
            Message msg = JSON.parseObject(messageStr, Message.class);
            msg.setSenderId(userId);
            msg.setType(1); // 默认 1 为私聊
            msg.setIsRead(0);
            msg.setCreateTime(LocalDateTime.now());
            
            // 消息持久化
            messageMapper.insert(msg);

            // 实时推送给目标用户（如果对方在线）
            sendToUser(msg.getReceiverId(), JSON.toJSONString(msg));
        } catch (Exception e) {
            log.error("处理 WebSocket 消息异常", e);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 发生错误", error);
    }

    /**
     * 服务端提供主动推送消息的 API（后续商品上架发系统通知可直接调用此方法）
     */
    public static void sendToUser(Long receiverId, String message) {
        Session session = sessionMap.get(receiverId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                log.error("推送消息给用户 {} 失败", receiverId, e);
            }
        }
    }
}