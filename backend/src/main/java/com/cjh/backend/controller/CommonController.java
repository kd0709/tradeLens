package com.cjh.backend.controller;

import com.alibaba.fastjson2.JSONObject;
import com.cjh.backend.annotation.ApiLog;
import com.cjh.backend.annotation.CurrentUser;
import com.cjh.backend.service.impl.AiRecognitionService;
import com.cjh.backend.service.FileStorageService;
import com.cjh.backend.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class CommonController {

    private final FileStorageService fileStorageService;
    private final AiRecognitionService aiRecognitionService;


    /**
     * 通用上传接口
     */
    @PostMapping("/upload")
    @ApiLog("上传文件到MinIO")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        String url = fileStorageService.upload(file);

        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.success(data);
    }

    /**
     * AI 智能填表接口
     */
    @GetMapping("/ai/fill-form")
    @ApiLog("AI智能识别商品")
    public Result<JSONObject> aiFillForm(
            @CurrentUser Long userId,
            @RequestParam("imageUrl") String imageUrl) {

        try {
            // 核心修改：不再去本地磁盘找文件。
            // 既然前端传过来的是 MinIO 的网络 URL (http://...)，我们直接把这个 URL 传给大模型即可。
            // 阿里千问(Qwen)等现代大模型 API 原生支持直接读取网络 URL 进行图像识别。
            JSONObject formJson = aiRecognitionService.recognizeProductAndFillForm(imageUrl);

            return Result.success(formJson);

        } catch (Exception e) {
            log.error("AI识别异常", e);
            return Result.fail(500, "智能识别失败：" + e.getMessage());
        }
    }
}