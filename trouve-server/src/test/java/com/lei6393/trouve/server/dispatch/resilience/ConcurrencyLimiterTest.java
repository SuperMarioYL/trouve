package com.lei6393.trouve.server.dispatch.resilience;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link ConcurrencyLimiter} 回归测试（v2.0）。
 *
 * @author trouve
 */
public class ConcurrencyLimiterTest {

    @After
    public void tearDown() {
        ConcurrencyLimiter.reset();
    }

    @Test
    public void unlimited_alwaysAcquires() {
        ConcurrencyLimiter.configure(0);
        assertFalse(ConcurrencyLimiter.isLimited());
        assertTrue(ConcurrencyLimiter.tryAcquire());
        assertTrue(ConcurrencyLimiter.tryAcquire());
        ConcurrencyLimiter.release();
        ConcurrencyLimiter.release();
    }

    @Test
    public void limited_rejectsWhenSaturated_thenRecovers() {
        ConcurrencyLimiter.configure(2);
        assertTrue(ConcurrencyLimiter.isLimited());

        assertTrue(ConcurrencyLimiter.tryAcquire());
        assertTrue(ConcurrencyLimiter.tryAcquire());
        // 饱和：第三个被拒
        assertFalse(ConcurrencyLimiter.tryAcquire());

        // 释放一个许可后可再次放行
        ConcurrencyLimiter.release();
        assertTrue(ConcurrencyLimiter.tryAcquire());

        ConcurrencyLimiter.release();
        ConcurrencyLimiter.release();
    }
}
