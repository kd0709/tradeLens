package com.cjh.backend.init;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cjh.backend.entity.Product;
import com.cjh.backend.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BloomFilterInit implements CommandLineRunner {

    private final RedissonClient redissonClient;
    private final ProductMapper productMapper;

    public static final String PRODUCT_BLOOM_KEY = "tradelens:product:bloom";

    @Override
    public void run(String... args) {
        log.info("========== 开始初始化商品布隆过滤器 ==========");
        
        // 1. 获取布隆过滤器实例
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(PRODUCT_BLOOM_KEY);
        
        // 2. 初始化配置 (预计数据量 10万，可接受误判率 0.01%)
        // 注意：如果已经初始化过，tryInit 会返回 false，不会重复覆盖
        bloomFilter.tryInit(100000L, 0.0001);

        // 3. 全量查询真实的商品 ID 并塞入过滤器
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Product::getId).eq(Product::getProductStatus, 1); // 假设 1 是正常上架状态
        List<Product> products = productMapper.selectList(queryWrapper);

        for (Product product : products) {
            bloomFilter.add(product.getId());
        }

        log.info("========== 布隆过滤器初始化完成，共加载 {} 个商品ID ==========", products.size());
    }
}