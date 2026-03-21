package com.cjh.backend.common;

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
public class BloomFilterCommon implements CommandLineRunner {

    private final RedissonClient redissonClient;
    private final ProductMapper productMapper;

    public static final String PRODUCT_BLOOM_KEY = "tradelens:product:bloom";

    @Override
    public void run(String... args) {
        log.info("初始化布隆过滤器");

        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(PRODUCT_BLOOM_KEY);

        if (bloomFilter.isExists()) {
            log.info("布隆过滤器已存在，跳过初始化");
            return;
        }

        long count = productMapper.selectCount(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getProductStatus, 1)
        );

        bloomFilter.tryInit(count, 0.0001);

        int pageSize = 1000;
        int page = 1;

        while (true) {
            List<Product> list = productMapper.selectList(
                    new LambdaQueryWrapper<Product>()
                            .select(Product::getId)
                            .eq(Product::getProductStatus, 1)
                            .last("limit " + (page - 1) * pageSize + "," + pageSize)
            );

            if (list.isEmpty()) break;

            list.forEach(p -> {
                if (p.getId() != null) {
                    bloomFilter.add(p.getId());
                }
            });

            page++;
        }

        log.info("布隆过滤器初始化完成");
    }
}