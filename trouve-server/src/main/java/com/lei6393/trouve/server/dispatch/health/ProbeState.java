package com.lei6393.trouve.server.dispatch.health;

/**
 * 单实例主动探活的滞回（hysteresis）状态机。
 * <p>
 * 健康态下连续失败达到 failThreshold 才转不健康；不健康态下连续成功达到 riseThreshold 才转健康。
 * 滞回避免在抖动上游上反复震荡。起始乐观（健康）。
 *
 * @author trouve
 */
public class ProbeState {

    private final int failThreshold;

    private final int riseThreshold;

    private int consecutiveFail = 0;

    private int consecutiveSuccess = 0;

    private volatile boolean healthy = true;

    public ProbeState(int failThreshold, int riseThreshold) {
        this.failThreshold = Math.max(1, failThreshold);
        this.riseThreshold = Math.max(1, riseThreshold);
    }

    public synchronized void recordSuccess() {
        consecutiveFail = 0;
        if (!healthy && ++consecutiveSuccess >= riseThreshold) {
            healthy = true;
            consecutiveSuccess = 0;
        }
    }

    public synchronized void recordFailure() {
        consecutiveSuccess = 0;
        if (healthy && ++consecutiveFail >= failThreshold) {
            healthy = false;
            consecutiveFail = 0;
        }
    }

    public boolean isHealthy() {
        return healthy;
    }
}
