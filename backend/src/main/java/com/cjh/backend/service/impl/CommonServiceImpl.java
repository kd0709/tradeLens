package com.cjh.backend.service.impl;

import com.cjh.backend.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class CommonServiceImpl implements CommonService {

    // 配置文件中设置，例如：file.upload.path=/data/uploads/
    @Value("${file.upload.path:/tmp/uploads/}")
    private String uploadPath;

    // 访问前缀，例如 http://localhost:8080/uploads/
    @Value("${file.access.prefix:http://localhost:8080/uploads/}")
    private String accessPrefix;

    @Override
    public String uploadFile(MultipartFile file, Long userId) {
        try {
            // 按日期 + 用户ID 分目录，避免文件过多冲突
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String userDir = "user_" + userId;
            String dirPath = uploadPath + datePath + "/" + userDir + "/";

            File dir = new File(dirPath);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    throw new IOException("创建目录失败：" + dirPath);
                }
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID() + ext;

            // 保存文件
            File dest = new File(dirPath + newFileName);
            file.transferTo(dest);

            // 返回访问URL
            String url = accessPrefix + datePath + "/" + userDir + "/" + newFileName;
            return url;

        } catch (IOException e) {
            log.error("文件保存异常", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }
}