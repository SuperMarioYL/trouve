package com.lei6393.trouve.server.dispatch.resilience;

import com.lei6393.trouve.core.data.instance.Instance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局熔断器注册表，按 instanceId 维护每个上游实例的 {@link CircuitBreaker}。
 * <p>
 * 默认<b>关闭</b>（{@link #enabled} = false），不改变既有转发行为；
 * 需通过 {@code @EnableTrouveDiscover} 的 dispatchHttpProperty 显式开启。
 *
 * @author trouve
 */
public final class CircuitBreakerRegistry {

    private static final ConcurrentHashMap<String, CircuitBreaker> BREAKERS = new ConcurrentHashMap<>();

    private static volatile boolean enabled = false;

    private static volatile int failureThreshold = 5;

    private static volatile long openDurationMillis = 10_000L;

    private CircuitBreakerRegistry() {
    }

    public static void configure(boolean enabled, int failureThreshold, long openDurationMillis) {
        CircuitBreakerRegistry.enabled = enabled;
        CircuitBreakerRegistry.failureThreshold = failureThreshold;
        CircuitBreakerRegistry.openDurationMillis = openDurationMillis;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    private static CircuitBreaker get(String instanceId) {
        return BREAKERS.computeIfAbsent(instanceId,
                key -> new CircuitBreaker(failureThreshold, openDurationMillis, System::currentTimeMillis));
    }

    public static boolean allows(String instanceId) {
        return !enabled || instanceId == null || get(instanceId).allowRequest();
    }

    public static void recordSuccess(String instanceId) {
        if (enabled && instanceId != null) {
            get(instanceId).recordSuccess();
        }
    }

    public static void recordFailure(String instanceId) {
        if (enabled && instanceId != null) {
            get(instanceId).recordFailure();
        }
    }

    /**
     * 过滤掉熔断器处于 OPEN 的实例。若过滤后为空（全部熔断），回退到原列表（fail-open），
     * 避免熔断状态把整条路由打成不可用。
     *
     * @param instances 候选实例
     * @return 允许放行的实例（或原列表）
     */
    public static List<Instance> filterAllowed(List<Instance> instances) {
        if (!enabled) {
            return instances;
        }
        List<Instance> allowed = new ArrayList<>(instances.size());
        for (Instance instance : instances) {
            if (allows(instance.getInstanceId())) {
                allowed.add(instance);
            }
        }
        return allowed.isEmpty() ? instances : allowed;
    }

    /**
     * 测试用：清空所有熔断器状态。
     */
    static void resetForTest() {
        BREAKERS.clear();
    }
}
