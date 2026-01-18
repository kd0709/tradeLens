package com.cjh.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductPublishDto {

    @NotNull
    private Long categoryId;

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private BigDecimal price;

    @NotNull
    private Integer quantity;

    @NotNull
    private Integer conditionLevel; // 1-4

    private Integer negotiable; // 1是 0否

    @NotEmpty
    private List<ProductImageDto> images;
}
