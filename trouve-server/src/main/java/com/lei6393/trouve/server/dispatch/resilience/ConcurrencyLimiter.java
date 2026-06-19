package com.lei6393.trouve.server.dispatch.resilience;

import java.util.concurrent.Semaphore;

/**
 * 网关入口并发限制器。
 * <p>
 * {@code EntranceController @RequestMapping("**")} 默认无界盲转发，突发流量可耗尽 OkHttp / Tomcat 线程
 * 拖垮整个网关。开启后限制同时在途的转发数，饱和时快速失败（503）削峰。
 * <p>
 * 默认<b>关闭</b>（maxConcurrent &lt;= 0 表示不限制），不改变既有行为。
 *
 * @author trouve
 */
public final class ConcurrencyLimiter {

    private static volatile Semaphore semaphore = null;

    private ConcurrencyLimiter() {
    }

    /**
     * @param maxConcurrent 最大并发在途请求数；&lt;= 0 表示不限制
     */
    public static void configure(int maxConcurrent) {
        semaphore = maxConcurrent > 0 ? new Semaphore(maxConcurrent) : null;
    }

    public static boolean isLimited() {
        return semaphore != null;
    }

    /**
     * 尝试获取一个许可。不限制时恒返回 true（不占用许可）。
     *
     * @return 是否获得放行
     */
    public static boolean tryAcquire() {
        Semaphore current = semaphore;
        return current == null || current.tryAcquire();
    }

    /**
     * 释放许可。仅在限制开启时实际归还，应与一次成功的 {@link #tryAcquire()} 配对调用。
     */
    public static void release() {
        Semaphore current = semaphore;
        if (current != null) {
            current.release();
        }
    }

    /**
     * 测试用：重置为不限制。
     */
    static void reset() {
        semaphore = null;
    }
}
