package com.lei6393.trouve.server.dispatch;

import org.junit.Test;
import org.springframework.http.HttpHeaders;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * {@link TraceHeaders} 追踪上下文判定回归测试（v2.1）。
 *
 * @author trouve
 */
public class TraceHeadersTest {

    @Test
    public void empty_hasNoTraceContext() {
        assertFalse(TraceHeaders.hasTraceContext(new HttpHeaders()));
    }

    @Test
    public void nullHeaders_hasNoTraceContext() {
        assertFalse(TraceHeaders.hasTraceContext(null));
    }

    @Test
    public void traceparent_detected() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("traceparent", "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01");
        assertTrue(TraceHeaders.hasTraceContext(headers));
    }

    @Test
    public void requestId_caseInsensitive() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Request-Id", "abc-123");
        assertTrue(TraceHeaders.hasTraceContext(headers));
    }

    @Test
    public void correlationId_nonNullAndUnique() {
        String a = TraceHeaders.newCorrelationId();
        String b = TraceHeaders.newCorrelationId();
        assertNotNull(a);
        assertNotEquals(a, b);
    }
}
