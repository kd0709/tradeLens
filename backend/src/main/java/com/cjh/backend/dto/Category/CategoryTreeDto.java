package com.cjh.backend.dto.Category;

import lombok.Data;
import java.util.List;

@Data
public class CategoryTreeDto {
    private Long id;
    private String name;
    private Long parentId;
    private Integer level;
    private List<CategoryTreeDto> children;  // 子分类列表
}
