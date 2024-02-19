package com.lei6393.trouve.server.loadbalance.policy;

import com.lei6393.trouve.core.data.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机负载均衡，不考虑权重
 *
 * @author leiyu
 * @date 2022/6/8 12:16
 */
public class RandomLoadBalancePolicy extends AbstractLoadBalancePolicy {

    @Override
    public Instance doSelect(@NotNull List<Instance> instances) {
        int length = instances.size();
        return instances.get(ThreadLocalRandom.current().nextInt(length));
    }
}
