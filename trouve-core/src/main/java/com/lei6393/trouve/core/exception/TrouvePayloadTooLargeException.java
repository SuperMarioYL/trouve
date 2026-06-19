package com.lei6393.trouve.core.exception;

/**
 * 请求体超过配置的最大转发体积时抛出，映射为 HTTP 413。
 *
 * @author trouve
 */
public class TrouvePayloadTooLargeException extends TrouveException {

    public TrouvePayloadTooLargeException() {
        super(TrouveErrorType.PAYLOAD_TOO_LARGE_ERROR);
    }
}
