package com.cjh.backend.aspect;

import com.cjh.backend.annotation.UserBehaviorTrack;
import com.cjh.backend.utils.Result; // 确保这里的 Result 路径与你项目一致
import com.cjh.backend.dto.Product.ProductDetailDto;
import com.cjh.backend.entity.Product;
import com.cjh.backend.mapper.ProductMapper;
import com.cjh.backend.mapper.UserPreferenceMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class BehaviorTrackAspect {

    @Autowired
    private UserPreferenceMapper userPreferenceMapper;

    @Autowired
    private ProductMapper productMapper;

    /**
     * 拦截所有标注了 @UserBehaviorTrack 的方法，在方法成功返回后异步执行
     */
    @Async
    @AfterReturning(value = "@annotation(track)", returning = "result")
    public void trackBehavior(JoinPoint joinPoint, UserBehaviorTrack track, Object result) {
        try {
            Long userId = null;
            Long productId = null;
            Long categoryId = null;

            // 1. 智能提取 userId
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                if (arg instanceof Long) {
                    userId = (Long) arg;
                }
            }

            // 如果用户未登录，直接跳过埋点
            if (userId == null) {
                return;
            }

            // 2. 智能提取 productId
            // 场景 A：从返回值中提取 (针对 VIEW，直接拿 ProductDetailDto 里的 id)
            if (result instanceof Result) {
                Object data = ((Result<?>) result).getData();
                if (data instanceof ProductDetailDto) {
                    productId = ((ProductDetailDto) data).getId();
                }
            }

            // 场景 B：从方法参数中提取 (针对 CART, FAVORITE 从 DTO 中拿，或路径参数里的 id)
            if (productId == null) {
                for (Object arg : args) {
                    // 如果参数是 Long 型且不是 userId，大概率是路径传过来的 productId
                    if (arg instanceof Long && !arg.equals(userId)) {
                        productId = (Long) arg;
                        break;
                    } else if (arg != null) {
                        try {
                            // 尝试调用参数对象的 getProductId() 方法
                            Method method = arg.getClass().getMethod("getProductId");
                            productId = (Long) method.invoke(arg);
                            break;
                        } catch (Exception ignored) {
                        }
                    }
                }
            }

            // 3. 核心修复：拿着 productId 去数据库查真正的 categoryId
            if (productId != null) {
                Product product = productMapper.selectById(productId);
                if (product != null) {
                    categoryId = product.getCategoryId();
                }
            }

            // 4. 记录分数到 user_preference 表
            if (categoryId != null) {
                userPreferenceMapper.addPreferenceScore(userId, categoryId, track.score());
                log.info("【个性化推荐埋点】用户ID: {}, 动作: {}, 目标商品ID: {}, 归属分类ID: {}, 增加积分: {}",
                        userId, track.action(), productId, categoryId, track.score());
            }

        } catch (Exception e) {
            log.error("【个性化推荐埋点】记录行为轨迹异常，但不影响主线业务", e);
        }
    }
}