package com.cjh.backend.utils;

import lombok.Data;

@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;


    private static final int SUCCESS_CODE = 200;
    public static final String SUCCESS_MESSAGE = "操作成功";

    public static final int FAIL_CODE = 400;
    public static final String FAIL_MESSAGE = "操作失败";


    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(SUCCESS_CODE, SUCCESS_MESSAGE, null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, SUCCESS_MESSAGE, data);
    }


    public static <T> Result<T> fail() {
        return new Result<>(FAIL_CODE, FAIL_MESSAGE, null);
    }

    public static <T> Result<T> fail(T data) {
        return new Result<>(FAIL_CODE, FAIL_MESSAGE, data);
    }




}
