package com.cjh.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class ProductStatusDto {

    @NotNull
    private Integer status; // 2上架 3下架
}
