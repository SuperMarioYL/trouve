package com.lei6393.trouve.server.consistency.singleton;

import com.lei6393.trouve.core.data.instance.Instance;
import com.lei6393.trouve.core.exception.TrouveException;
import com.lei6393.trouve.server.consistency.AbstractHealthChecker;
import com.lei6393.trouve.server.instence.InstanceOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yulei
 * @date 2022/5/25 11:19
 */
public class SingletonInstanceHealthChecker extends AbstractHealthChecker implements InstanceOperator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SingletonInstanceHealthChecker.class);

    private volatile Map<String, Long> healthMapping = new ConcurrentHashMap<>();

    public SingletonInstanceHealthChecker(long period, long timeoutDuration) {
        super(period, timeoutDuration);
    }

    @Override
    public Set<String> flushHealthInstanceIds() {
        long expire = System.currentTimeMillis() - getTimeoutDuration();
        Set<String> healthInstanceIds = new HashSet<>();
        Set<String> unHealthInstanceIds = new HashSet<>();
        for (Map.Entry<String, Long> entry : healthMapping.entrySet()) {
            if (entry.getValue() < expire || Objects.isNull(entry.getValue())) {
                unHealthInstanceIds.add(entry.getKey());
            } else {
                healthInstanceIds.add(entry.getKey());
            }
        }
        for (String instanceId : unHealthInstanceIds) {
            healthMapping.remove(instanceId);
        }
        return healthInstanceIds;
    }

    @Override
    public void registerInstance(String serviceName, Instance instance) throws TrouveException {
        healthMapping.put(instance.getInstanceId(), System.currentTimeMillis());
    }


    @Override
    public void removeInstance(String serviceName, Instance instance) throws TrouveException {
        healthMapping.remove(instance.getInstanceId());
    }

    @Override
    public void updateInstance(String serviceName, Instance instance) throws TrouveException {
        healthMapping.put(instance.getInstanceId(), System.currentTimeMillis());
    }
}
