package com.lei6393.trouve.core.exception;

import java.text.MessageFormat;

/**
 * @author yulei
 * @date 2022/5/22 09:43
 */
public class TrouveException extends Exception {

    private int code;

    private String message;

    public TrouveException(TrouveErrorType errorType) {
        super(MessageFormat.format("code: {0}, message: {1}",
                String.valueOf(errorType.getCode()), errorType.getMessage()));
        this.code = errorType.getCode();
        this.message = errorType.getMessage();
    }

    public TrouveException(int code, String message) {
        super(MessageFormat.format("code: {0}, message: {1}", String.valueOf(code), message));
        this.code = code;
        this.message = message;
    }

    public TrouveException() {
        super(MessageFormat.format("code: {0}, message: {1}",
                String.valueOf(TrouveErrorType.DEFAULT_ERROR.getCode()), TrouveErrorType.DEFAULT_ERROR.getMessage()));
        this.code = TrouveErrorType.DEFAULT_ERROR.getCode();
        this.message = TrouveErrorType.DEFAULT_ERROR.getMessage();
    }

    public TrouveException(String message) {
        super(MessageFormat.format("code: {0}, message: {1}",
                String.valueOf(TrouveErrorType.DEFAULT_ERROR.getCode()), message));
        this.code = TrouveErrorType.DEFAULT_ERROR.getCode();
        this.message = message;
    }

}
