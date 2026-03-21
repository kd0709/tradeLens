package com.cjh.backend.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类（增强版）
 * 特点：
 * 1. 支持泛型
 * 2. 支持自增/自减
 * 3. 支持过期时间查询
 * 4. 支持分布式锁（简单版）
 * 5. 日志规范
 */
@Slf4j
@Component
public class RedisUtils {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ============================= Common =============================

    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                return Boolean.TRUE.equals(redisTemplate.expire(key, time, TimeUnit.SECONDS));
            }
            return false;
        } catch (Exception e) {
            log.error("设置过期时间失败 key={}", key, e);
            return false;
        }
    }

    public long getExpire(String key) {
        try {
            Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return expire == null ? -1 : expire;
        } catch (Exception e) {
            log.error("获取过期时间失败 key={}", key, e);
            return -1;
        }
    }

    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("判断key是否存在失败 key={}", key, e);
            return false;
        }
    }

    public void del(String... keys) {
        if (keys != null && keys.length > 0) {
            try {
                if (keys.length == 1) {
                    redisTemplate.delete(keys[0]);
                } else {
                    redisTemplate.delete(Arrays.asList(keys));
                }
            } catch (Exception e) {
                log.error("删除key失败 keys={}", Arrays.toString(keys), e);
            }
        }
    }

    // ============================= String =============================

    public <T> T get(String key) {
        try {
            return (T) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取缓存失败 key={}", key, e);
            return null;
        }
    }

    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("设置缓存失败 key={}", key, e);
            return false;
        }
    }

    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error("设置缓存失败 key={}", key, e);
            return false;
        }
    }

    // ============================= 自增/自减 =============================

    public long incr(String key, long delta) {
        if (delta < 0) throw new RuntimeException("递增因子必须大于0");
        return redisTemplate.opsForValue().increment(key, delta);
    }

    public long decr(String key, long delta) {
        if (delta < 0) throw new RuntimeException("递减因子必须大于0");
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    // ============================= Hash =============================

    public <T> T hget(String key, String item) {
        return (T) redisTemplate.opsForHash().get(key, item);
    }

    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) expire(key, time);
            return true;
        } catch (Exception e) {
            log.error("hmset失败 key={}", key, e);
            return false;
        }
    }

    public boolean hset(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            log.error("hset失败 key={}", key, e);
            return false;
        }
    }

    public void hdel(String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
    }

    // ============================= Set =============================

    public Set<Object> sGet(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public long sSet(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    public boolean sHasKey(String key, Object value) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
    }

    // ============================= List =============================

    public List<Object> lGet(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    public boolean lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            log.error("lSet失败 key={}", key, e);
            return false;
        }
    }

    public boolean lSet(String key, List<Object> values) {
        try {
            redisTemplate.opsForList().rightPushAll(key, values);
            return true;
        } catch (Exception e) {
            log.error("批量lSet失败 key={}", key, e);
            return false;
        }
    }

    // ============================= 分布式锁（基础版） =============================

    public boolean tryLock(String key, String value, long timeout) {
        try {
            return Boolean.TRUE.equals(
                    redisTemplate.opsForValue().setIfAbsent(key, value, timeout, TimeUnit.SECONDS)
            );
        } catch (Exception e) {
            log.error("加锁失败 key={}", key, e);
            return false;
        }
    }

    public void unlock(String key, String value) {
        try {
            Object current = redisTemplate.opsForValue().get(key);
            if (value.equals(current)) {
                redisTemplate.delete(key);
            }
        } catch (Exception e) {
            log.error("释放锁失败 key={}", key, e);
        }
    }
}