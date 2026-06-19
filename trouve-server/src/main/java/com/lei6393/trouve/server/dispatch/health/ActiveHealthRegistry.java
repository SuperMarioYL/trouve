package com.lei6393.trouve.server.dispatch.health;

import com.lei6393.trouve.core.data.instance.Instance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 主动探活结果注册表，按 instanceId 维护 {@link ProbeState}。
 * <p>
 * 默认<b>关闭</b>。开启后 {@link ActiveHealthProber} 周期 HTTP 探活并记录结果，
 * 转发候选选择时摘除被判不健康的实例（全部不健康则 fail-open）。
 *
 * @author trouve
 */
public final class ActiveHealthRegistry {

    private static final ConcurrentHashMap<String, ProbeState> STATES = new ConcurrentHashMap<>();

    private static volatile boolean enabled = false;

    private static volatile int failThreshold = 3;

    private static volatile int riseThreshold = 2;

    private ActiveHealthRegistry() {
    }

    public static void configure(boolean enabled, int failThreshold, int riseThreshold) {
        ActiveHealthRegistry.enabled = enabled;
        ActiveHealthRegistry.failThreshold = failThreshold;
        ActiveHealthRegistry.riseThreshold = riseThreshold;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    private static ProbeState state(String instanceId) {
        return STATES.computeIfAbsent(instanceId, key -> new ProbeState(failThreshold, riseThreshold));
    }

    public static void recordSuccess(String instanceId) {
        if (enabled && instanceId != null) {
            state(instanceId).recordSuccess();
        }
    }

    public static void recordFailure(String instanceId) {
        if (enabled && instanceId != null) {
            state(instanceId).recordFailure();
        }
    }

    public static boolean isHealthy(String instanceId) {
        return !enabled || instanceId == null || state(instanceId).isHealthy();
    }

    /**
     * 过滤掉主动探活判定为不健康的实例；全部不健康时回退原列表（fail-open）。
     */
    public static List<Instance> filterHealthy(List<Instance> instances) {
        if (!enabled) {
            return instances;
        }
        List<Instance> healthy = new ArrayList<>(instances.size());
        for (Instance instance : instances) {
            if (isHealthy(instance.getInstanceId())) {
                healthy.add(instance);
            }
        }
        return healthy.isEmpty() ? instances : healthy;
    }

    /**
     * 测试用：清空状态。
     */
    static void resetForTest() {
        STATES.clear();
    }
}
