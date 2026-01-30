package com.cjh.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface CommonService {

    /**
     * 上传文件，返回可访问的URL
     * @param file 文件
     * @param userId 当前用户ID（可用于生成路径前缀、日志等）
     * @return 访问URL
     */
    String uploadFile(MultipartFile file, Long userId);
}