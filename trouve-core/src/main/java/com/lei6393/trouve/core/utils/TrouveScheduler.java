package com.lei6393.trouve.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * 统一的定时调度工具。
 * <p>
 * 取代 {@link java.util.Timer}：Timer 是单线程且任务抛出任何未捕获异常会<b>永久</b>终止其线程，
 * 导致心跳 / 健康检查 / 路由刷新等被静默停掉且不可恢复。这里用命名的守护线程
 * {@link ScheduledExecutorService}，并通过 {@link #guard(String, Runnable)} 对每个任务做
 * 错误隔离（吞掉并记录 {@link Throwable}），保证单次失败不影响后续周期。
 *
 * @author trouve
 */
public final class TrouveScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrouveScheduler.class);

    private TrouveScheduler() {
    }

    /**
     * 创建单线程的命名守护调度器。
     *
     * @param name 线程名
     * @return 调度器
     */
    public static ScheduledExecutorService newSingleThread(String name) {
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable, name);
            thread.setDaemon(true);
            return thread;
        };
        return Executors.newSingleThreadScheduledExecutor(threadFactory);
    }

    /**
     * 包装一个周期任务，捕获并记录 {@link Throwable}，避免异常向上传播导致
     * {@link ScheduledExecutorService} 取消后续调度。
     *
     * @param desc 任务描述（用于日志）
     * @param task 实际任务
     * @return 错误隔离后的任务
     */
    public static Runnable guard(String desc, Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (Throwable t) {
                LOGGER.error("scheduled task [{}] failed, will retry next period", desc, t);
            }
        };
    }
}
