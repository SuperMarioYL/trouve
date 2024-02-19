package com.lei6393.trouve.server.consistency.nonsingleton.redis;

import com.lei6393.trouve.core.data.instance.Instance;
import com.lei6393.trouve.core.exception.TrouveException;
import com.lei6393.trouve.server.TrouveLoader;
import com.lei6393.trouve.server.consistency.AbstractHealthChecker;
import com.lei6393.trouve.server.instence.InstanceOperator;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * @author yulei
 * @date 2022/5/25 11:19
 */
public class RedisInstanceHealthChecker extends AbstractHealthChecker implements InstanceOperator {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisInstanceHealthChecker.class);

    private static final String HEALTH_INSTANCE_SET_KEY = "trouve_health_instance_set_";

    private final RedissonClient rClient;

    public RedisInstanceHealthChecker(RedissonClient rClient, long period, long timeoutDuration) {
        super(period, timeoutDuration);
        this.rClient = rClient;
    }

    @Override
    public Set<String> flushHealthInstanceIds() {
        long expire = System.currentTimeMillis() - getTimeoutDuration();
        Set<String> healthInstanceIds = readHealthInstanceIds(getKey(), expire);
        // 从Redis删除过期实例
        removeHealthInstanceIds(getKey(), expire);

        return healthInstanceIds;
    }

    @Override
    public void registerInstance(String serviceName, Instance instance) throws TrouveException {
        RScoredSortedSet<String> scoredSortedSet = rClient.getScoredSortedSet(getKey());
        scoredSortedSet.add(System.currentTimeMillis(), instance.getInstanceId());
    }


    @Override
    public void removeInstance(String serviceName, Instance instance) throws TrouveException {
        RScoredSortedSet<String> scoredSortedSet = rClient.getScoredSortedSet(getKey());
        scoredSortedSet.remove(instance.getInstanceId());
    }

    @Override
    public void updateInstance(String serviceName, Instance instance) throws TrouveException {
        RScoredSortedSet<String> scoredSortedSet = rClient.getScoredSortedSet(getKey());
        Double score = scoredSortedSet.getScore(instance.getInstanceId());
        if (Objects.isNull(score)) {
            registerInstance(serviceName, instance);
        } else {
            scoredSortedSet.addScore(instance.getInstanceId(), System.currentTimeMillis() - score);
        }
    }

    public String getKey() {
        return HEALTH_INSTANCE_SET_KEY + TrouveLoader.getNamespace();
    }

    private Set<String> readHealthInstanceIds(String key, long expire) {
        RScoredSortedSet<String> scoredSortedSet = rClient.getScoredSortedSet(key);
        Collection<String> instanceIds = scoredSortedSet.valueRange(expire, true, Double.POSITIVE_INFINITY, true);
        return (Set<String>) instanceIds;
    }

    private void removeHealthInstanceIds(String key, long expire) {
        RScoredSortedSet<String> scoredSortedSet = rClient.getScoredSortedSet(key);
        int count = scoredSortedSet.removeRangeByScore(Double.NEGATIVE_INFINITY, true, expire, false);
    }

}
