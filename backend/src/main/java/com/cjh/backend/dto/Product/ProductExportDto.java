package com.cjh.backend.dto.Product;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductExportDto {
    @ExcelProperty("商品ID")
    private Long id;

    @ExcelProperty("发布者ID")
    private Long userId;

    @ExcelProperty("分类ID")
    private Long categoryId;

    @ExcelProperty("商品标题")
    private String title;

    @ExcelProperty("商品价格")
    private BigDecimal price;

    @ExcelProperty("商品数量")
    private Integer quantity;

    @ExcelProperty("新旧程度(1全新 2近新 3轻微 4明显)")
    private Integer conditionLevel;

    @ExcelProperty("是否可议价(1是 0否)")
    private Integer negotiable;

    @ExcelProperty("状态(1待审核 2上架 3下架 4已售)")
    private Integer productStatus;

    @ExcelProperty("浏览量")
    private Integer viewCount;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}