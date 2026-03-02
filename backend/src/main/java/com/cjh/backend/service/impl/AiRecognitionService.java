package com.cjh.backend.service.impl;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

@Slf4j
@Service
public class AiRecognitionService {

    @Value("${alibaba.dashscope.api-key}")
    private String apiKey;

    public JSONObject recognizeProductAndFillForm(String imagePath) {
        try {

            File file = new File(imagePath);
            if (!file.exists()) {
                throw new RuntimeException("图片文件不存在");
            }

            // 1️⃣ 读取文件转 Base64
            String base64Image = encodeToBase64(file);

            MultiModalConversation conv = new MultiModalConversation();

            String prompt = """
                    你是一个二手交易平台的智能助手。
                    请分析这张图片中的商品，并生成二手发布信息。
                    严格只返回一个JSON对象，不要Markdown，不要解释。

                    JSON字段：
                    title: 商品标题(20字内)
                    description: 商品描述(50字左右)
                    price: 合理二手价格(数字)
                    categoryName: 分类(手机数码,家用电器,服饰鞋包,美妆个护,运动户外,其他)
                    conditionLevel: 成色(1全新,2几乎全新,3轻微使用,4明显使用)
                    """;

            MultiModalMessage message = MultiModalMessage.builder()
                    .role(Role.USER.getValue())
                    .content(Arrays.asList(
                            Map.of("image", "data:image/jpeg;base64," + base64Image),
                            Map.of("text", prompt)
                    ))
                    .build();

            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(apiKey)
                    .model("qwen-vl-max") // 用最新模型
                    .messages(Collections.singletonList(message))
                    .build();

            MultiModalConversationResult result = conv.call(param);

            String aiResponse = extractText(result);

            log.info("AI原始返回: {}", aiResponse);

            return JSON.parseObject(aiResponse);

        } catch (Exception e) {
            log.error("阿里云AI识别失败", e);
            throw new RuntimeException("智能识别失败：" + e.getMessage());
        }
    }

    // 提取 text 内容（更安全）
    private String extractText(MultiModalConversationResult result) {
        List<?> contents = result.getOutput()
                .getChoices()
                .get(0)
                .getMessage()
                .getContent();

        for (Object obj : contents) {
            if (obj instanceof Map map && map.containsKey("text")) {
                return map.get("text").toString()
                        .replace("```json", "")
                        .replace("```", "")
                        .trim();
            }
        }

        throw new RuntimeException("AI未返回文本内容");
    }

    // 文件转 Base64
    private String encodeToBase64(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        byte[] bytes = fis.readAllBytes();
        fis.close();
        return Base64.getEncoder().encodeToString(bytes);
    }
}