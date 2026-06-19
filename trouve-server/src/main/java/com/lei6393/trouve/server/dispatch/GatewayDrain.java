package com.lei6393.trouve.server.dispatch;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 网关优雅停机协调器。
 * <p>
 * 进入 draining 后，入口拒绝新请求（503）、等待在途转发完成，再让应用退出，避免在途请求被切断。
 *
 * @author trouve
 */
public final class GatewayDrain {

    private static volatile boolean draining = false;

    private static final AtomicInteger IN_FLIGHT = new AtomicInteger(0);

    private GatewayDrain() {
    }

    public static boolean isDraining() {
        return draining;
    }

    public static void startDraining() {
        draining = true;
    }

    public static void enter() {
        IN_FLIGHT.incrementAndGet();
    }

    public static void exit() {
        IN_FLIGHT.decrementAndGet();
    }

    public static int inFlight() {
        return IN_FLIGHT.get();
    }

    /**
     * 等待在途请求降为 0，或超时。
     *
     * @param timeoutMillis 最长等待毫秒
     * @return 是否在超时前完成 drain
     */
    public static boolean awaitDrain(long timeoutMillis) {
        long deadline = System.currentTimeMillis() + Math.max(0, timeoutMillis);
        while (IN_FLIGHT.get() > 0) {
            if (System.currentTimeMillis() >= deadline) {
                return false;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return IN_FLIGHT.get() == 0;
            }
        }
        return true;
    }

    /**
     * 测试用：重置状态。
     */
    static void resetForTest() {
        draining = false;
        IN_FLIGHT.set(0);
    }
}
