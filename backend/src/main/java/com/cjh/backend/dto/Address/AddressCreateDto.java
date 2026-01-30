// com.cjh.backend.dto.address.AddressCreateDto.java
package com.cjh.backend.dto.Address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;


@Data
public class AddressCreateDto {

    @NotBlank(message = "收货人姓名不能为空")
    @Length(max = 20, message = "收货人姓名过长")
    private String receiverName;

    @NotBlank(message = "收货人电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String receiverPhone;

    @NotBlank(message = "省份不能为空")
    private String province;

    @NotBlank(message = "城市不能为空")
    private String city;

    @NotBlank(message = "区/县不能为空")
    private String district;

    @NotBlank(message = "详细地址不能为空")
    @Length(max = 100, message = "详细地址过长")
    private String detailAddress;

    private Integer isDefault = 0;  // 默认 0，非必填，1表示设为默认
}