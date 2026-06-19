package com.lei6393.trouve.server.consistency;

import com.lei6393.trouve.core.utils.TrouveScheduler;
import com.lei6393.trouve.server.meta.MetaOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 路由匹配表周期刷新器。
 * <p>
 * v1.3 变更：{@link java.util.Timer} -> 命名守护 {@link ScheduledExecutorService}，
 * 单次刷新异常不再永久终止刷新线程。
 *
 * @author leiyu
 * @date 2022/5/26 12:20
 */
public abstract class AbstractMatcherUpdator implements Runnable, MetaOperator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMatcherUpdator.class);

    private final AbstractHealthChecker healthChecker;

    private final ScheduledExecutorService scheduler = TrouveScheduler.newSingleThread("trouve-matcher-updator");

    public AbstractMatcherUpdator(AbstractHealthChecker healthChecker, long flushDuration) {
        this.healthChecker = healthChecker;
        this.scheduler.scheduleWithFixedDelay(
                TrouveScheduler.guard("matcher-flush", this), 0, flushDuration, TimeUnit.MILLISECONDS);
    }

    public abstract void flushMatcher() throws Exception;

    @Override
    public void run() {
        try {
            flushMatcher();
        } catch (Exception e) {
            LOGGER.error("flush matcher error!", e);
        }
    }

    public AbstractHealthChecker getHealthChecker() {
        return healthChecker;
    }
}
