package com.cjh.backend.dto.Comment;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentExportDto {
    @ExcelProperty("评价ID")
    private Long id;

    @ExcelProperty("评价用户ID")
    private Long userId;

    @ExcelProperty("商品ID")
    private Long productId;

    @ExcelProperty("订单ID")
    private Long orderId;

    @ExcelProperty("评分(1-5)")
    private Integer score;

    @ExcelProperty("评价内容")
    private String content;

    @ExcelProperty("评价时间")
    private LocalDateTime createTime;
}