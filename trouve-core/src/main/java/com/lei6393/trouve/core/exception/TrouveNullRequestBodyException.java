package com.lei6393.trouve.core.exception;

/**
 * @author yulei
 * @date 2022/7/8 15:37
 */
public class TrouveNullRequestBodyException extends TrouveException {

    public TrouveNullRequestBodyException() {
        super(TrouveErrorType.REQUEST_BODY_NULL_ERROR);
    }
}
