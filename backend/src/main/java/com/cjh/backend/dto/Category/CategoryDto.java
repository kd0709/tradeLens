package com.cjh.backend.dto.Category;

import lombok.Data;

@Data
public class CategoryDto {
    private Long id;
    private String name;
    private Long parentId;
    private Integer level;
}
