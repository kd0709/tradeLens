package com.cjh.backend.service.impl;

import com.cjh.backend.service.FileStorageService;
import com.cjh.backend.exception.BusinessException; // 假设您有自定义异常，如果没有可换成 RuntimeException
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local") // 仅在配置为 local 时生效
public class LocalFileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        // 1. 准备目录
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 2. 生成新文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + suffix;

        // 3. 保存文件
        try {
            file.transferTo(new File(dir, newFileName));
            log.info("文件已保存至本地: {}", newFileName);
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new RuntimeException("文件上传失败");
        }

        // 4. 返回 Web 访问路径 (注意：这里只返回相对路径)
        return "/images/" + newFileName;
    }
}