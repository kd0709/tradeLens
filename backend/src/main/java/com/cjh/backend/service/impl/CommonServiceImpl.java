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

    @Value("${file.upload.path:/tmp/uploads/}")
    private String uploadPath;

    @Value("${file.access.prefix:http://localhost:8080/uploads/}")
    private String accessPrefix;

    @Override
    public String uploadFile(MultipartFile file, Long userId) {
        try {
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String userDir = "user_" + userId;
            String dirPath = uploadPath + datePath + "/" + userDir + "/";

            File dir = new File(dirPath);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IOException("创建目录失败：" + dirPath);
            }

            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID() + ext;

            File dest = new File(dirPath + newFileName);
            file.transferTo(dest);

            return accessPrefix + datePath + "/" + userDir + "/" + newFileName;

        } catch (IOException e) {
            log.error("文件保存异常", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }
}