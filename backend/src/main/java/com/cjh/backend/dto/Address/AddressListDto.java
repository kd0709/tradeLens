// com.cjh.backend.dto.address.AddressListDto.java
package com.cjh.backend.dto.Address;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AddressListDto {
    private Long id;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private Integer isDefault;      // 1=默认 0=非默认
    private LocalDateTime createTime;
}