package com.lei6393.trouve.server.dispatch.metrics;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 转发链路的轻量内置指标（无外部依赖）。
 * <p>
 * 作为可观测性的第一步，记录请求 / 上游响应 / 转发失败(502/504) / 重试 / 限流拒绝计数，
 * 通过 {@code /trouve/manager/metrics} 暴露。后续可替换为 Micrometer/Prometheus。
 *
 * @author trouve
 */
public final class DispatchMetrics {

    private static final AtomicLong REQUESTS = new AtomicLong();

    private static final AtomicLong UPSTREAM_RESPONSES = new AtomicLong();

    private static final AtomicLong FORWARD_FAILURES = new AtomicLong();

    private static final AtomicLong RETRIES = new AtomicLong();

    private static final AtomicLong REJECTED = new AtomicLong();

    private DispatchMetrics() {
    }

    /** 进入转发的请求数（已通过限流）。 */
    public static void recordRequest() {
        REQUESTS.incrementAndGet();
    }

    /** 拿到上游真实响应（任意状态码）的次数。 */
    public static void recordUpstreamResponse() {
        UPSTREAM_RESPONSES.incrementAndGet();
    }

    /** 所有尝试均失败、产出 502/504 的次数。 */
    public static void recordForwardFailure() {
        FORWARD_FAILURES.incrementAndGet();
    }

    /** 发生重试（故障转移）的次数。 */
    public static void recordRetry() {
        RETRIES.incrementAndGet();
    }

    /** 因并发限流被拒绝（503）的次数。 */
    public static void recordRejected() {
        REJECTED.incrementAndGet();
    }

    public static Map<String, Long> snapshot() {
        Map<String, Long> map = new LinkedHashMap<>();
        map.put("requests", REQUESTS.get());
        map.put("upstreamResponses", UPSTREAM_RESPONSES.get());
        map.put("forwardFailures", FORWARD_FAILURES.get());
        map.put("retries", RETRIES.get());
        map.put("rejected", REJECTED.get());
        return map;
    }

    /**
     * 以 Prometheus 文本曝光格式输出（零依赖，可直接被 Prometheus 抓取）。
     */
    public static String prometheus() {
        StringBuilder sb = new StringBuilder();
        appendCounter(sb, "trouve_requests_total", "Requests entering dispatch", REQUESTS.get());
        appendCounter(sb, "trouve_upstream_responses_total", "Upstream responses received", UPSTREAM_RESPONSES.get());
        appendCounter(sb, "trouve_forward_failures_total", "All-attempts-failed forwards (502/504)", FORWARD_FAILURES.get());
        appendCounter(sb, "trouve_retries_total", "Retries via failover", RETRIES.get());
        appendCounter(sb, "trouve_rejected_total", "Requests rejected by concurrency limit", REJECTED.get());
        return sb.toString();
    }

    private static void appendCounter(StringBuilder sb, String name, String help, long value) {
        sb.append("# HELP ").append(name).append(' ').append(help).append('\n');
        sb.append("# TYPE ").append(name).append(" counter\n");
        sb.append(name).append(' ').append(value).append('\n');
    }

    /** 测试用：清零。 */
    public static void reset() {
        REQUESTS.set(0);
        UPSTREAM_RESPONSES.set(0);
        FORWARD_FAILURES.set(0);
        RETRIES.set(0);
        REJECTED.set(0);
    }
}
