package com.cjh.backend.utils;

import lombok.Data;

@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;

    // 默认状态码和消息
    private static final int SUCCESS_CODE = 200;
    private static final String SUCCESS_MESSAGE = "操作成功";

    private static final int FAIL_CODE = 400;
    private static final String FAIL_MESSAGE = "操作失败";

    // 私有构造
    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ================== 成功方法 ==================

    public static <T> Result<T> success() {
        return new Result<>(SUCCESS_CODE, SUCCESS_MESSAGE, null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, SUCCESS_MESSAGE, data);
    }

    public static <T> Result<T> success(String message) {
        return new Result<>(SUCCESS_CODE, message, null);
    }

    public static <T> Result<T> success(T data, String message) {
        return new Result<>(SUCCESS_CODE, message, data);
    }

    public static <T> Result<T> success(int code, String message, T data) {
        return new Result<>(code, message, data);
    }

    // ================== 失败方法 ==================

    public static <T> Result<T> fail() {
        return new Result<>(FAIL_CODE, FAIL_MESSAGE, null);
    }

    public static <T> Result<T> fail(T data) {
        return new Result<>(FAIL_CODE, FAIL_MESSAGE, data);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(FAIL_CODE, message, null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> fail(int code, String message, T data) {
        return new Result<>(code, message, data);
    }

    // ================== 链式调用支持 ==================
    public Result<T> message(String message) {
        this.message = message;
        return this;
    }

    public Result<T> data(T data) {
        this.data = data;
        return this;
    }

    public Result<T> code(int code) {
        this.code = code;
        return this;
    }
}
