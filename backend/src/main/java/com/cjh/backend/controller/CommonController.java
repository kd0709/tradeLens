package com.cjh.backend.controller;

import com.alibaba.fastjson2.JSONObject;
import com.cjh.backend.common.CurrentUser;
import com.cjh.backend.dto.UploadDto;
import com.cjh.backend.service.impl.AiRecognitionService;
import com.cjh.backend.service.FileStorageService;
import com.cjh.backend.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class CommonController {

    private final FileStorageService fileStorageService;

    // 1. 必须注入我们刚刚写的 AI 服务
    private final AiRecognitionService aiRecognitionService;

    @PostMapping("/upload")
    public Result<UploadDto> upload(
            @RequestPart("file") MultipartFile file,
            @CurrentUser Long userId) {

        try {
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

    // 2. 新增 AI 智能填表的接口
    @GetMapping("/ai/fill-form")
    public Result<JSONObject> aiFillForm(
            @CurrentUser Long userId,
            @RequestParam("imageUrl") String imageUrl) {
        log.info("用户 {} 触发AI智能识别填表，图片: {}", userId, imageUrl);
        try {
            // 调用通义千问获取结果
            JSONObject formJson = aiRecognitionService.recognizeProductAndFillForm("https://images.unsplash.com/photo-1511707171634-5f897ff02aa9");
            //JSONObject formJson = aiRecognitionService.recognizeProductAndFillForm(imageUrl);
            return Result.success(formJson);
        } catch (Exception e) {
            return Result.fail(500, "智能识别失败：" + e.getMessage());
        }
    }
}