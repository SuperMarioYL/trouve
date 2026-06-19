package com.lei6393.trouve.server.dispatch.resilience;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link CircuitBreaker} 状态机回归测试（v2.0），时钟注入保证确定性。
 *
 * @author trouve
 */
public class CircuitBreakerTest {

    private CircuitBreaker breaker(AtomicLong clock, int threshold, long openMillis) {
        return new CircuitBreaker(threshold, openMillis, clock::get);
    }

    @Test
    public void opensAfterThresholdFailures() {
        AtomicLong clock = new AtomicLong(0);
        CircuitBreaker cb = breaker(clock, 3, 1000);

        assertTrue(cb.allowRequest());
        cb.recordFailure();
        cb.recordFailure();
        assertEquals(CircuitBreaker.State.CLOSED, cb.state());
        assertTrue(cb.allowRequest());

        cb.recordFailure(); // 第 3 次 -> OPEN
        assertEquals(CircuitBreaker.State.OPEN, cb.state());
        assertFalse(cb.allowRequest()); // 冷却期内拒绝
    }

    @Test
    public void halfOpenAfterCooldown_thenCloseOnSuccess() {
        AtomicLong clock = new AtomicLong(0);
        CircuitBreaker cb = breaker(clock, 2, 1000);

        cb.recordFailure();
        cb.recordFailure(); // OPEN
        assertFalse(cb.allowRequest());

        clock.set(1000); // 冷却期满
        assertTrue(cb.allowRequest()); // 转 HALF_OPEN 放行试探
        assertEquals(CircuitBreaker.State.HALF_OPEN, cb.state());

        cb.recordSuccess(); // 试探成功 -> CLOSED
        assertEquals(CircuitBreaker.State.CLOSED, cb.state());
        assertTrue(cb.allowRequest());
    }

    @Test
    public void halfOpenFailure_reopens() {
        AtomicLong clock = new AtomicLong(0);
        CircuitBreaker cb = breaker(clock, 2, 1000);

        cb.recordFailure();
        cb.recordFailure(); // OPEN
        clock.set(1000);
        assertTrue(cb.allowRequest()); // HALF_OPEN

        cb.recordFailure(); // 试探失败 -> 重新 OPEN
        assertEquals(CircuitBreaker.State.OPEN, cb.state());
        assertFalse(cb.allowRequest());
    }

    @Test
    public void successResetsFailureStreak() {
        AtomicLong clock = new AtomicLong(0);
        CircuitBreaker cb = breaker(clock, 3, 1000);

        cb.recordFailure();
        cb.recordFailure();
        cb.recordSuccess(); // 重置连续失败计数
        cb.recordFailure();
        cb.recordFailure();
        assertEquals(CircuitBreaker.State.CLOSED, cb.state()); // 仍未达阈值
    }
}
