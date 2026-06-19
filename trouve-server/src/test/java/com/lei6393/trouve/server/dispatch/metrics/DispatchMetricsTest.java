package com.lei6393.trouve.server.dispatch.metrics;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * {@link DispatchMetrics} 计数回归测试（v2.1）。
 *
 * @author trouve
 */
public class DispatchMetricsTest {

    @Before
    public void reset() {
        DispatchMetrics.reset();
    }

    @Test
    public void counters_accumulate() {
        DispatchMetrics.recordRequest();
        DispatchMetrics.recordRequest();
        DispatchMetrics.recordUpstreamResponse();
        DispatchMetrics.recordRetry();
        DispatchMetrics.recordForwardFailure();
        DispatchMetrics.recordRejected();

        Map<String, Long> snap = DispatchMetrics.snapshot();
        assertEquals(Long.valueOf(2), snap.get("requests"));
        assertEquals(Long.valueOf(1), snap.get("upstreamResponses"));
        assertEquals(Long.valueOf(1), snap.get("retries"));
        assertEquals(Long.valueOf(1), snap.get("forwardFailures"));
        assertEquals(Long.valueOf(1), snap.get("rejected"));
    }

    @Test
    public void reset_zeroesAll() {
        DispatchMetrics.recordRequest();
        DispatchMetrics.reset();
        for (Long value : DispatchMetrics.snapshot().values()) {
            assertEquals(Long.valueOf(0), value);
        }
    }

    @Test
    public void prometheus_exposesCounters() {
        DispatchMetrics.recordRequest();
        DispatchMetrics.recordForwardFailure();
        String out = DispatchMetrics.prometheus();
        assertTrue(out.contains("# TYPE trouve_requests_total counter"));
        assertTrue(out.contains("trouve_requests_total 1"));
        assertTrue(out.contains("trouve_forward_failures_total 1"));
        assertTrue(out.contains("trouve_rejected_total 0"));
    }
}
