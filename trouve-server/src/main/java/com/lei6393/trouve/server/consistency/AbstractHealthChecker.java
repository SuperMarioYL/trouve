package com.lei6393.trouve.server.consistency;

import com.lei6393.trouve.core.data.instance.Instance;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 实例健康检查类
 *
 * @author leiyu
 * @date 2022/5/26 11:32
 */
public abstract class AbstractHealthChecker extends TimerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractHealthChecker.class);

    private volatile Set<String> healthInstanceIds;

    private final long timeoutDuration;

    private final Timer timer = new Timer();

    public AbstractHealthChecker(long period, long timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
        this.timer.schedule(this, 0, period);
    }

    public boolean filterHealthInstance(Set<Instance> instances) {
        return instances.removeIf(ready -> !checkHealth(ready));
    }

    public boolean checkHealth(Instance instance) {
        return checkHealth(instance.getInstanceId());
    }

    public boolean checkHealth(String instanceId) {
        if (Objects.isNull(instanceId)) {
            return false;
        } else if (CollectionUtils.isEmpty(healthInstanceIds)) {
            return true;
        } else {
            return healthInstanceIds.contains(instanceId);
        }
    }

    /**
     * The action to be performed by this timer task.
     */
    @Override
    public void run() {
        try {
            this.healthInstanceIds = flushHealthInstanceIds();
        } catch (Exception e) {
            LOGGER.error("get latest health instance ids is error!", e);
        }
    }

    public abstract Set<String> flushHealthInstanceIds();

    public long getTimeoutDuration() {
        return timeoutDuration;
    }

    public Set<String> getHealthInstanceIds() {
        return healthInstanceIds;
    }
}
