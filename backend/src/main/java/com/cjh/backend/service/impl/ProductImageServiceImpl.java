package com.cjh.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjh.backend.entity.ProductImage;
import com.cjh.backend.service.ProductImageService;
import com.cjh.backend.mapper.ProductImageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
* @author 45209
* @description 针对表【product_image(商品图片表)】的数据库操作Service实现
* @createDate 2026-01-29 18:58:49
*/
@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl extends ServiceImpl<ProductImageMapper, ProductImage>
    implements ProductImageService{

}




