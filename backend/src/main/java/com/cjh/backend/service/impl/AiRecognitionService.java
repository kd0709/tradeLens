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

import java.util.Arrays;
import java.util.Collections;

@Slf4j
@Service
public class AiRecognitionService {

    @Value("${alibaba.dashscope.api-key}")
    private String apiKey;

    /**
     * 识别图片并返回前端表单所需的完整 JSON
     */
    public JSONObject recognizeProductAndFillForm(String imageUrl) {
        try {
            MultiModalConversation conv = new MultiModalConversation();
            
            // 构造系统提示词，要求严格返回 JSON
            String prompt = "你是一个二手交易平台的智能助手。请分析这张图片中的商品，并生成二手发布信息。\n" +
                    "请严格只返回一个JSON对象，不要有任何Markdown格式或多余的文字说明。\n" +
                    "JSON字段要求：\n" +
                    "1. title: 商品标题，突出品牌型号和卖点(20字内)\n" +
                    "2. description: 商品的详细二手描述，猜测一下使用场景和状态(50字左右)\n" +
                    "3. price: 预估的合理二手价格(数字)\n" +
                    "4. categoryName: 猜测的分类(如:手机数码,家用电器,服饰鞋包,美妆个护,运动户外,其他)\n" +
                    "5. conditionLevel: 预估成色(1:全新, 2:几乎全新, 3:轻微使用, 4:明显使用)";

            // 构造消息体
            MultiModalMessage message = MultiModalMessage.builder()
                    .role(Role.USER.getValue())
                    .content(Arrays.asList(
                            Collections.singletonMap("image", imageUrl),
                            Collections.singletonMap("text", prompt)
                    ))
                    .build();

            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(apiKey)
                    .model("qwen-vl-plus") // 使用通义千问视觉模型
                    .messages(Collections.singletonList(message))
                    .build();

            MultiModalConversationResult result = conv.call(param);
            String aiResponse = result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text").toString();
            
            // 清理可能带有的 markdown 标记 (例如 ```json ... ```)
            aiResponse = aiResponse.replace("```json", "").replace("```", "").trim();
            
            return JSON.parseObject(aiResponse);

        } catch (Exception e) {
            log.error("阿里云AI商品识别失败", e);
            throw new RuntimeException("智能识别失败，请手动填写");
        }
    }
}