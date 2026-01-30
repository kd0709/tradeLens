// com.cjh.backend.dto.user.SellerInfoDto.java
package com.cjh.backend.dto.User;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SellerInfoDto {
    private Long id;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    private LocalDateTime createTime;     // 可选：显示“加入时间”
    private Integer productCount;         // 可选：发布的商品数（统计用）
    // 如果后续想加交易完成数、好评率等，可以在这里扩展
}