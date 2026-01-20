package com.cjh.backend.exception;

/**
 * 业务异常类
 * 用于表示业务逻辑中的错误情况
 */
public class BusinessException extends RuntimeException {

    private int code;
    private String message;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
        this.message = message;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
