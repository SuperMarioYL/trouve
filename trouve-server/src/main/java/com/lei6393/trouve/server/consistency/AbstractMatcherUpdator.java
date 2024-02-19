package com.lei6393.trouve.server.consistency;

import com.lei6393.trouve.server.meta.MetaOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author yulei
 * @date 2022/5/26 12:20
 */
public abstract class AbstractMatcherUpdator extends TimerTask implements MetaOperator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMatcherUpdator.class);

    private final AbstractHealthChecker healthChecker;

    private final Timer timer = new Timer();

    public AbstractMatcherUpdator(AbstractHealthChecker healthChecker, long flushDuration) {
        this.healthChecker = healthChecker;
        this.timer.schedule(this, 0, flushDuration);
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
