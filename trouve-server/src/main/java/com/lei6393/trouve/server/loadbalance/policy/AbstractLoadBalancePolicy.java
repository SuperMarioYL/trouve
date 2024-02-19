package com.lei6393.trouve.server.loadbalance.policy;

import com.lei6393.trouve.core.Constants;
import com.lei6393.trouve.core.data.instance.Instance;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author leiyu
 * @date 2022/6/8 11:55
 */
public abstract class AbstractLoadBalancePolicy implements LoadBalancePolicy {

    /**
     * 运用负载均衡策略挑选实例
     *
     * @param instances
     * @return
     */
    @Override
    public Instance select(@NotNull List<Instance> instances) {
        if (CollectionUtils.isEmpty(instances)) {
            return null;
        }
        if (CollectionUtils.size(instances) == 1) {
            return instances.get(0);
        }
        return doSelect(instances);
    }

    protected abstract Instance doSelect(@NotNull List<Instance> instances);

    protected int getWeight(Instance instance) {
        int weight = instance.getWeight();
        weight = Integer.max(Constants.MIN_WEIGHT_VALUE, weight);
        weight = Integer.min(Constants.MAX_WEIGHT_VALUE, weight);
        return weight;
    }
}
