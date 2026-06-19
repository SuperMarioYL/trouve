package com.lei6393.trouve.server.dispatch.health;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link ProbeState} 滞回状态机回归测试（v2.x 主动探活）。
 *
 * @author trouve
 */
public class ProbeStateTest {

    @Test
    public void startsHealthy() {
        assertTrue(new ProbeState(3, 2).isHealthy());
    }

    @Test
    public void goesUnhealthyOnlyAtFailThreshold() {
        ProbeState state = new ProbeState(3, 2);
        state.recordFailure();
        state.recordFailure();
        assertTrue(state.isHealthy()); // 未达阈值
        state.recordFailure();
        assertFalse(state.isHealthy()); // 第 3 次 -> 不健康
    }

    @Test
    public void successResetsFailStreak_hysteresis() {
        ProbeState state = new ProbeState(3, 2);
        state.recordFailure();
        state.recordFailure();
        state.recordSuccess(); // 重置失败计数
        state.recordFailure();
        state.recordFailure();
        assertTrue(state.isHealthy()); // 仍未连续达到 3 次
    }

    @Test
    public void recoversOnlyAtRiseThreshold() {
        ProbeState state = new ProbeState(2, 2);
        state.recordFailure();
        state.recordFailure(); // -> 不健康
        assertFalse(state.isHealthy());

        state.recordSuccess();
        assertFalse(state.isHealthy()); // 1 次成功不足以恢复
        state.recordSuccess();
        assertTrue(state.isHealthy()); // 连续 2 次 -> 恢复
    }

    @Test
    public void failureResetsSuccessStreakDuringRecovery() {
        ProbeState state = new ProbeState(2, 3);
        state.recordFailure();
        state.recordFailure(); // 不健康
        state.recordSuccess();
        state.recordSuccess();
        state.recordFailure(); // 打断恢复
        state.recordSuccess();
        state.recordSuccess();
        assertFalse(state.isHealthy()); // 仍未连续 3 次成功
        state.recordSuccess();
        assertTrue(state.isHealthy());
    }
}
