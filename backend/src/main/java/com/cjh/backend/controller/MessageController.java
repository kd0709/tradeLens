package com.cjh.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cjh.backend.annotation.CurrentUser;
import com.cjh.backend.entity.Message;
import com.cjh.backend.mapper.MessageMapper;
import com.cjh.backend.service.UserService;
import com.cjh.backend.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private UserService userService;


    /**
     * 获取未读消息总数（用于前端展示红点）
     */
    @GetMapping("/unread/count")
    public Result<Long> getUnreadCount(@CurrentUser Long userId) {
        Long count = messageMapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getReceiverId, userId)
                        .eq(Message::getIsRead, 0)
        );
        return Result.success(count);
    }

    /**
     * 获取与特定用户的聊天历史 (targetId=0 可视为拉取系统通知)
     */
    @GetMapping("/history/{targetId}")
    public Result<List<Message>> getHistory(@CurrentUser Long userId, @PathVariable Long targetId) {
        List<Message> list = messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .and(w -> w.eq(Message::getSenderId, userId).eq(Message::getReceiverId, targetId))
                        .or(w -> w.eq(Message::getSenderId, targetId).eq(Message::getReceiverId, userId))
                        .orderByAsc(Message::getCreateTime)
        );
        return Result.success(list);
    }



    /**
     *  将某人发给我的消息一键标为已读（消除红点）
     */
    @PutMapping("/read/{senderId}")
    public Result<Void> markAsRead(@CurrentUser Long userId, @PathVariable Long senderId) {
        messageMapper.update(null, 
                new LambdaUpdateWrapper<Message>()
                        .set(Message::getIsRead, 1)
                        .eq(Message::getReceiverId, userId)
                        .eq(Message::getSenderId, senderId)
                        .eq(Message::getIsRead, 0)
        );
        return Result.success();
    }

    /**
     * 获取当前用户的最近聊天联系人列表 (包含未读数)
     */
    @GetMapping("/contacts")
    public Result<java.util.List<java.util.Map<String, Object>>> getContacts(@CurrentUser Long userId) {
        // 1. 查询所有与我有过聊天的记录，按时间倒序
        java.util.List<Message> messages = messageMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Message>()
                        .eq(Message::getSenderId, userId)
                        .or()
                        .eq(Message::getReceiverId, userId)
                        .orderByDesc(Message::getCreateTime)
        );

        // 2. 提取去重的联系人ID (LinkedHashSet 保证按最近聊天时间排序)
        java.util.Set<Long> contactIds = new java.util.LinkedHashSet<>();
        contactIds.add(0L); // 始终把系统通知(ID为0)包含在联系人中
        for (Message msg : messages) {
            Long contactId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();
            contactIds.add(contactId);
        }

        // 3. 组装返回数据
        java.util.List<java.util.Map<String, Object>> resultList = new java.util.ArrayList<>();
        for (Long id : contactIds) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", id);
            if (id == 0L) {
                map.put("nickname", "系统通知");
                map.put("avatar", "");
            } else {
                com.cjh.backend.entity.User u = userService.getById(id);
                if (u != null) {
                    map.put("nickname", u.getNickname());
                    map.put("avatar", u.getAvatar());
                } else {
                    continue; // 忽略已删除的用户
                }
            }
            // 4. 统计该联系人发给我的未读数
            Long unread = messageMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Message>()
                            .eq(Message::getSenderId, id)
                            .eq(Message::getReceiverId, userId)
                            .eq(Message::getIsRead, 0)
            );
            map.put("unread", unread);
            resultList.add(map);
        }
        return Result.success(resultList);
    }
}