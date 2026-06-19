package com.lei6393.trouve.server.dispatch;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 转发重试次数回归测试（v1.2）。
 * <p>
 * 修复前 {@code callTimes = 1 + Math.min(retryCount, 0)} 恒为 1，重试永不生效；
 * 修复后 {@code totalAttempts = 1 + Math.max(retryCount, 0)}。
 *
 * @author trouve
 */
public class DispatchRetryTest {

    @Test
    public void totalAttempts_default_isOne() {
        // retryCount=0 -> 仅 1 次首发，不重试
        assertEquals(1, AbstractDispatchCenterProcessor.totalAttempts(0));
    }

    @Test
    public void totalAttempts_positive_addsRetries() {
        // retryCount=3 -> 1 次首发 + 3 次重试 = 4
        assertEquals(4, AbstractDispatchCenterProcessor.totalAttempts(3));
    }

    @Test
    public void totalAttempts_negative_clampedToOne() {
        // 负值按 0 处理，至少保证 1 次调用（旧 bug 下正数 retry 会退化为 0）
        assertEquals(1, AbstractDispatchCenterProcessor.totalAttempts(-5));
    }

    @Test
    public void bodyLimit_zeroMeansUnlimited() {
        assertFalse(AbstractDispatchCenterProcessor.exceedsBodyLimit(Long.MAX_VALUE, 0));
        assertFalse(AbstractDispatchCenterProcessor.exceedsBodyLimit(-1, 0));
    }

    @Test
    public void bodyLimit_enforcedWhenConfigured() {
        assertTrue(AbstractDispatchCenterProcessor.exceedsBodyLimit(2048, 1024));
        assertFalse(AbstractDispatchCenterProcessor.exceedsBodyLimit(1024, 1024));
        assertFalse(AbstractDispatchCenterProcessor.exceedsBodyLimit(512, 1024));
        // Content-Length 未知(-1) 时不拦截（best-effort，留待真正的流式限制）
        assertFalse(AbstractDispatchCenterProcessor.exceedsBodyLimit(-1, 1024));
    }
}
