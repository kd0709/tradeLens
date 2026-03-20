package com.cjh.backend.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenBlacklist {

    private final RedisUtils redisUtils;

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    /**
     * 加入黑名单
     * @param token JWT
     * @param remainSeconds 剩余存活时间 (秒)
     */
    public void addToBlacklist(String token, long remainSeconds) {
        if (remainSeconds > 0) {
            redisUtils.set(BLACKLIST_PREFIX + token, "invalid", remainSeconds);
        }
    }

    /**
     * 检查是否在黑名单中
     * @param token JWT
     */
    public boolean contains(String token) {
        return redisUtils.hasKey(BLACKLIST_PREFIX + token);
    }
}