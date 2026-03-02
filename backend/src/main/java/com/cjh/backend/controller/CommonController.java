package com.cjh.backend.controller;

import com.alibaba.fastjson2.JSONObject;
import com.cjh.backend.common.CurrentUser;
import com.cjh.backend.dto.UploadDto;
import com.cjh.backend.service.impl.AiRecognitionService;
import com.cjh.backend.service.FileStorageService;
import com.cjh.backend.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;


@Slf4j
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class CommonController {

    private final FileStorageService fileStorageService;
    private final AiRecognitionService aiRecognitionService;

    // 注入配置文件中定义的本地存储基路径
    @Value("${file.upload-dir}")
    private String uploadPath;

    /**
     * 通用上传接口
     */
    @PostMapping("/upload")
    public Result<UploadDto> upload(
            @RequestPart("file") MultipartFile file,
            @CurrentUser Long userId) {

        try {
            // 上传文件并获取保存后的相对文件名或URL
            String url = fileStorageService.upload(file);

            UploadDto resp = new UploadDto();
            resp.setUrl(url);
            resp.setOriginalName(file.getOriginalFilename());
            resp.setSize(file.getSize());

            return Result.success(resp, "上传成功");
        } catch (Exception e) {
            log.error("用户 {} 上传失败", userId, e);
            return Result.fail(500, "上传失败：" + e.getMessage());
        }
    }

    /**
     * AI 智能填表接口
     * 修改重点：将前端传来的 URL 转换为后端磁盘的绝对路径
     */
    @GetMapping("/ai/fill-form")
    public Result<JSONObject> aiFillForm(
            @CurrentUser Long userId,
            @RequestParam("imageUrl") String imageUrl) {

        try {

            // 只取文件名，防止目录穿透
            String fileName = new File(imageUrl).getName();
            String physicalPath = uploadPath + File.separator + fileName;

            File imageFile = new File(physicalPath);
            if (!imageFile.exists()) {
                return Result.fail(404, "图片不存在");
            }

            JSONObject formJson = aiRecognitionService
                    .recognizeProductAndFillForm(physicalPath);

            return Result.success(formJson);

        } catch (Exception e) {
            log.error("AI识别异常", e);
            return Result.fail(500, "智能识别失败：" + e.getMessage());
        }
    }
}