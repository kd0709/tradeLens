package com.cjh.backend.controller;

import com.cjh.backend.common.CurrentUser;
import com.cjh.backend.dto.UploadDto;
import com.cjh.backend.service.FileStorageService;
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

    private final FileStorageService fileStorageService; // 注入接口

    @PostMapping("/upload")
    public Result<UploadDto> upload(
            @RequestPart("file") MultipartFile file,
            @CurrentUser Long userId) {

        try {
            // 调用接口上传，无需关心是本地还是云端
            String url = fileStorageService.upload(file);

            UploadDto resp = new UploadDto();
            resp.setUrl(url);
            resp.setOriginalName(file.getOriginalFilename());
            resp.setSize(file.getSize());

            return Result.success(resp, "上传成功");
        } catch (Exception e) {
            return Result.fail(500, "上传失败：" + e.getMessage());
        }
    }
}