package com.lei6393.trouve.core.exception;

/**
 * @author yulei
 * @date 2022/7/1 18:21
 */
public enum TrouveErrorType {

    DEFAULT_ERROR(282000, "trouve internal error"),
    EMPTY_INSTANCE_ERROR(282001, "trouve instance is empty"),
    UNREGISTERED_URL_ERROR(282002, "url not register"),

    REQUEST_BODY_NULL_ERROR(282003, "request body require not null"),

    ;
    private int code;

    private String message;

    TrouveErrorType(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
