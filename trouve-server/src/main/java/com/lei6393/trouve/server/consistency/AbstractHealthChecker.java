package com.lei6393.trouve.server.consistency;

import com.lei6393.trouve.core.data.instance.Instance;
import com.lei6393.trouve.core.utils.TrouveScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 实例健康检查类。
 * <p>
 * v1.3 变更：
 * <ul>
 *     <li>{@link java.util.Timer} -> 命名守护 {@link ScheduledExecutorService}，单次刷新异常不再终止后续检查；</li>
 *     <li>修复 fail-open：首次 flush 完成前（healthInstanceIds 为 null）才放行；一旦初始化过，
 *     即便健康集合为空也按"无健康实例"处理，而不是把所有实例当健康。</li>
 * </ul>
 *
 * @author leiyu
 * @date 2022/5/26 11:32
 */
public abstract class AbstractHealthChecker implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractHealthChecker.class);

    /**
     * 当前健康实例 id 集合。null 表示尚未完成首次刷新（未初始化）。
     */
    private volatile Set<String> healthInstanceIds;

    private final long timeoutDuration;

    private final ScheduledExecutorService scheduler = TrouveScheduler.newSingleThread("trouve-health-checker");

    public AbstractHealthChecker(long period, long timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
        this.scheduler.scheduleWithFixedDelay(
                TrouveScheduler.guard("health-check", this), 0, period, TimeUnit.MILLISECONDS);
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
        }
        Set<String> ids = healthInstanceIds;
        if (Objects.isNull(ids)) {
            // 首次刷新完成前放行，避免启动期所有实例被判不健康而全部 503
            return true;
        }
        // 已初始化：以健康集合为准（空集合表示当前无健康实例）
        return ids.contains(instanceId);
    }

    /**
     * The action to be performed by this scheduled task.
     */
    @Override
    public void run() {
        try {
            this.healthInstanceIds = flushHealthInstanceIds();
        } catch (Exception e) {
            // 保留上次成功的健康集合，避免一次刷新失败把所有实例打成不健康
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
