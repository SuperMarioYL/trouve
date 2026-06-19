package com.lei6393.trouve.server.dispatch;

import org.springframework.http.HttpHeaders;

import java.util.UUID;

/**
 * 分布式追踪上下文透传辅助。
 * <p>
 * 转发时所有请求头（含 W3C {@code traceparent} / B3 {@code b3}）原样透传；
 * 当请求不含任何追踪上下文时，生成一个 {@code X-Request-Id} 关联 id 一并转发，便于端到端日志关联。
 *
 * @author trouve
 */
public final class TraceHeaders {

    public static final String TRACEPARENT = "traceparent";

    public static final String B3 = "b3";

    public static final String REQUEST_ID = "X-Request-Id";

    private TraceHeaders() {
    }

    /**
     * 请求是否已带追踪上下文（traceparent / b3 / X-Request-Id 任一）。HttpHeaders 大小写不敏感。
     */
    public static boolean hasTraceContext(HttpHeaders headers) {
        return headers != null
                && (headers.containsKey(TRACEPARENT) || headers.containsKey(B3) || headers.containsKey(REQUEST_ID));
    }

    public static String newCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
