package com.cjh.backend.service;
import org.springframework.web.multipart.MultipartFile;


public interface FileStorageService {
    /**
     * 上传文件
     * @param file 文件对象
     * @return 文件的访问路径 (本地模式返回相对路径，OSS模式返回绝对路径)
     */
    String upload(MultipartFile file);
}