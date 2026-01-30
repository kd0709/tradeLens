package com.cjh.backend.dto;

import lombok.Data;

@Data
public class UploadDto {
    private String url;         // 上传后的访问地址
    private String originalName; // 原文件名
    private Long size;          // 文件大小（字节）
}