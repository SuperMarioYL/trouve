package com.lei6393.trouve.server.dispatch.resilience;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;

/**
 * 单实例熔断器（基于连续失败计数）。
 * <p>
 * 状态机：
 * <ul>
 *     <li>{@code CLOSED}：正常放行；连续失败达到阈值 -> {@code OPEN}。</li>
 *     <li>{@code OPEN}：直接拒绝；冷却时间到后下一次探测进入 {@code HALF_OPEN}。</li>
 *     <li>{@code HALF_OPEN}：放行一次试探，成功 -> {@code CLOSED}，失败 -> 重新 {@code OPEN}。</li>
 * </ul>
 * 时钟通过 {@link LongSupplier} 注入，便于确定性测试。
 *
 * @author trouve
 */
public class CircuitBreaker {

    public enum State {
        CLOSED, OPEN, HALF_OPEN
    }

    private final int failureThreshold;

    private final long openDurationMillis;

    private final LongSupplier clock;

    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);

    private volatile State state = State.CLOSED;

    private volatile long openedAt = 0L;

    public CircuitBreaker(int failureThreshold, long openDurationMillis, LongSupplier clock) {
        this.failureThreshold = Math.max(1, failureThreshold);
        this.openDurationMillis = Math.max(0, openDurationMillis);
        this.clock = clock;
    }

    /**
     * 是否允许本次请求通过。OPEN 状态下冷却期满会转入 HALF_OPEN 并放行一次试探。
     */
    public boolean allowRequest() {
        if (state == State.OPEN) {
            if (clock.getAsLong() - openedAt >= openDurationMillis) {
                state = State.HALF_OPEN;
                return true;
            }
            return false;
        }
        return true;
    }

    public void recordSuccess() {
        consecutiveFailures.set(0);
        state = State.CLOSED;
    }

    public void recordFailure() {
        int failures = consecutiveFailures.incrementAndGet();
        if (state == State.HALF_OPEN || failures >= failureThreshold) {
            state = State.OPEN;
            openedAt = clock.getAsLong();
        }
    }

    public State state() {
        return state;
    }
}
