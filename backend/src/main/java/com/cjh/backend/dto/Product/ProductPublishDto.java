package com.cjh.backend.dto.Product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import java.math.BigDecimal;
import java.util.List;


@Data
public class ProductPublishDto {

    @NotBlank(message = "标题不能为空")
    @Length(max = 100, message = "标题过长")
    private String title;

    private String description;  // 可选，TEXT 类型

    @NotNull(message = "价格不能为空")
    @Min(value = 0, message = "价格不能为负")
    private BigDecimal price;

    @NotNull(message = "分类ID不能为空")
    @Min(value = 1, message = "分类ID无效")
    private Long categoryId;

    private List<String> images;  // 图片 URL 数组，前端先上传后传 URL

    @NotNull(message = "成色等级不能为空")
    @Range(min = 1, max = 4, message = "成色等级无效")
    private Integer conditionLevel;

    private Integer quantity = 1;  // 默认 1

    private Integer negotiable = 1;  // 默认可议价

    private Integer isDeleted = 0;

    private Integer viewPoint = 1;
}