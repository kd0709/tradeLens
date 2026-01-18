package com.cjh.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCreateDTO {

    @NotNull
    private Long productId;
}
