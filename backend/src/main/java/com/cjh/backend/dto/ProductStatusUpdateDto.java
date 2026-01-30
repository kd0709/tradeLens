package com.cjh.backend.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;


@Data
public class ProductStatusUpdateDto {

    @NotNull(message = "商品ID不能为空")
    @Min(value = 1, message = "商品ID无效")
    private Long id;

    @NotNull(message = "状态不能为空")
    @Range(min = 1, max = 5, message = "状态值无效")
    private Integer status;  // 参考文档：2 上架 5 下架（可扩展其他状态）
}