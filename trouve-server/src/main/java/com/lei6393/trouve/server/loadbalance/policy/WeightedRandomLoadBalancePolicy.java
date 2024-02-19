package com.lei6393.trouve.server.loadbalance.policy;

import com.lei6393.trouve.core.data.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 加权随机负载均衡策略
 * 默认策略
 *
 * @author yulei
 * @date 2022/6/8 11:56
 */
public class WeightedRandomLoadBalancePolicy extends AbstractLoadBalancePolicy {


    @Override
    public Instance doSelect(@NotNull List<Instance> instances) {
        final int length = instances.size();

        boolean sameWeight = true;

        int[] weights = new int[length];

        int totalWeight = 0;
        for (int i = 0; i < length; i++) {
            int weight = getWeight(instances.get(i));

            totalWeight += weight;

            weights[i] = totalWeight;

            if (sameWeight && totalWeight != weight * (i + 1)) {
                sameWeight = false;
            }
        }

        if (totalWeight > 0 && !sameWeight) {
            int offset = ThreadLocalRandom.current().nextInt(totalWeight);
            for (int index = 0; index < length; index++) {
                if (offset < weights[index]) {
                    return instances.get(index);
                }
            }
        }
        return instances.get(ThreadLocalRandom.current().nextInt(length));
    }
}
