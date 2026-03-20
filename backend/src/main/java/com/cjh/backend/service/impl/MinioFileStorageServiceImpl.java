package com.cjh.backend.service.impl;

import com.cjh.backend.config.MinioConfig;
import com.cjh.backend.exception.BusinessException;
import com.cjh.backend.service.FileStorageService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Primary // 告诉 Spring 优先使用这个实现类
@Service
public class MinioFileStorageServiceImpl implements FileStorageService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfig minioConfig;

    @Override
    public String upload(MultipartFile file) {
        try {
            String bucketName = minioConfig.getBucketName();

            // 1. 检查桶是否存在，不存在则创建
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                // 注意：在实际环境或初始化脚本中，还需要为这个桶设置公开只读策略（Public Read），否则前端无法直接访问图片URL
            }

            // 2. 提取文件后缀
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 3. 构建高可用企业级文件路径：日期目录/UUID.后缀 (防止单目录文件过多)
            String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            String newFileName = datePath + "/" + UUID.randomUUID().toString().replace("-", "") + ext;

            // 4. 将文件流推送到 MinIO
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(newFileName)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            // 5. 返回对象的可访问绝对 URL
            return minioConfig.getEndpoint() + "/" + bucketName + "/" + newFileName;

        } catch (Exception e) {
            log.error("【MinIO】文件上传失败", e);
            throw new BusinessException("文件上传至云存储失败，请重试");
        }
    }
}