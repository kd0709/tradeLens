package com.cjh.backend.controller;

import com.cjh.backend.common.CurrentUser;
import com.cjh.backend.dto.UploadDto;
import com.cjh.backend.service.CommonService;
import com.cjh.backend.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class CommonController {

    private final CommonService commonService;


    @PostMapping("/upload")
    public Result<UploadDto> upload(
            @RequestPart("file") MultipartFile file,
            @CurrentUser Long userId) {

        if (file == null || file.isEmpty()) {
            log.warn("用户 {} 尝试上传空文件", userId);
            return Result.fail("文件不能为空");
        }

        log.info("用户 {} 开始上传文件：{}，大小：{} bytes", 
                 userId, file.getOriginalFilename(), file.getSize());

        try {
            String url = commonService.uploadFile(file, userId);
            
            UploadDto resp = new UploadDto();
            resp.setUrl(url);
            resp.setOriginalName(file.getOriginalFilename());
            resp.setSize(file.getSize());

            log.info("用户 {} 上传文件成功：{} → {}", 
                     userId, file.getOriginalFilename(), url);
                     
            return Result.success(resp, "上传成功");
            
        } catch (Exception e) {
            log.error("用户 {} 上传文件失败：{}，原因：{}", 
                      userId, file.getOriginalFilename(), e.getMessage(), e);
            return Result.fail(500, "上传失败：" + e.getMessage());
        }
    }
}