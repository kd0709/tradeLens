package com.cjh.backend.aspect;

import com.cjh.backend.annotation.DesensitizeField;
import com.cjh.backend.common.DesensitizeType;
import com.cjh.backend.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Collection;

@Slf4j
@Aspect
@Component
public class DesensitizeAspect {

    @AfterReturning(pointcut = "@annotation(com.cjh.backend.annotation.Desensitize)", returning = "resultObj")
    public void doDesensitize(Object resultObj) {
        if (resultObj == null) return;
        
        try {
            if (resultObj instanceof Result) {
                Object data = ((Result<?>) resultObj).getData();
                if (data == null) return;
                
                if (data instanceof Collection) {
                    for (Object item : (Collection<?>) data) {
                        processFields(item);
                    }
                } else {
                    processFields(data);
                }
            }
        } catch (Exception e) {
            log.error("【AOP数据脱敏】处理异常", e);
        }
    }

    private void processFields(Object obj) throws IllegalAccessException {
        if (obj == null) return;
        
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            if (field.isAnnotationPresent(DesensitizeField.class)) {
                field.setAccessible(true);
                Object value = field.get(obj);
                
                if (value instanceof String) {
                    String strValue = (String) value;
                    DesensitizeField annotation = field.getAnnotation(DesensitizeField.class);
                    String desensitizedValue = maskData(strValue, annotation.type());
                    field.set(obj, desensitizedValue);
                }
            }
        }
    }

    private String maskData(String data, DesensitizeType type) {
        if (data == null || data.isEmpty()) return data;
        
        switch (type) {
            case PHONE:
                return data.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
            case CHINESE_NAME:
                if (data.length() <= 1) return data;
                return data.substring(0, 1) + "**";
            case EMAIL:
                return data.replaceAll("(^.)[^@]*(@.*$)", "$1***$2");
            case ID_CARD:
                return data.replaceAll("(\\d{4})\\d{10}(\\w{4})", "$1**********$2");
            default:
                return data;
        }
    }
}