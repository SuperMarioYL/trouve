package com.lei6393.trouve.core.exception;

/**
 * @author yulei
 * @date 2022/7/1 18:30
 */
public class TrouveUnregisteredUrlException extends TrouveException {

    public TrouveUnregisteredUrlException(String pattern) {
        super(TrouveErrorType.UNREGISTERED_URL_ERROR.getCode(), "url " + pattern + " not register");
    }

    public TrouveUnregisteredUrlException() {
        super(TrouveErrorType.UNREGISTERED_URL_ERROR);
    }
}
