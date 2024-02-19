package com.lei6393.trouve.core.exception;

/**
 * @author leiyu
 * @date 2022/7/1 18:27
 */
public class TrouveEmptyInstanceException extends TrouveException {


    public TrouveEmptyInstanceException() {
        super(TrouveErrorType.EMPTY_INSTANCE_ERROR);
    }
}
