package com.cjh.backend.annotation;

/**
 * 脱敏策略枚举
 */
public enum DesensitizeType {
    /**
     * 手机号脱敏 (保留前3后4，如 138****5678)
     */
    PHONE,
    
    /**
     * 姓名脱敏 (保留姓氏，如 陈**)
     */
    CHINESE_NAME,
    
    /**
     * 邮箱脱敏 (隐藏@前面的部分字符，如 c***@qq.com)
     */
    EMAIL,
    
    /**
     * 身份证号脱敏 (保留前4后4)
     */
    ID_CARD
}