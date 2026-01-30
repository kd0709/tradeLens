// com.cjh.backend.dto.address.AddressUpdateDto.java
package com.cjh.backend.dto.Address;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddressUpdateDto {

    @NotNull(message = "地址ID不能为空")
    @Min(value = 1, message = "地址ID无效")
    private Long id;

    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private Integer isDefault;  // 1=设为默认，0=取消默认，其他值不处理
}