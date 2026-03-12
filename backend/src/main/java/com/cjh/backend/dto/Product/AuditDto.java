package com.cjh.backend.dto.Product;

import lombok.Data;

@Data
public  class AuditDto {

    private Long productId;
    private Boolean pass;
    private String reason;
}