package com.cjh.backend.dto.Category;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CategoryExportDto {
    @ExcelProperty("分类ID")
    private Long id;

    @ExcelProperty("分类名称")
    private String name;

    @ExcelProperty("父分类ID")
    private Long parentId;

    @ExcelProperty("分类层级")
    private Integer level;

    @ExcelProperty("分类路径")
    private String path;

    @ExcelProperty("状态(1启用 0禁用)")
    private Integer status;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}