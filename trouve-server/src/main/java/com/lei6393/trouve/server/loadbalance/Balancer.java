package com.lei6393.trouve.server.loadbalance;

import com.lei6393.trouve.core.data.instance.Instance;
import com.lei6393.trouve.core.exception.TrouveException;
import com.lei6393.trouve.server.loadbalance.policy.LoadBalancePolicy;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * 负载均衡器
 *
 * @author leiyu
 * @date 2022/5/25 22:22
 */
public class Balancer {

    private static LoadBalancePolicy policy;

    public static void defineLoadBalancePolicy(@NotNull LoadBalancePolicy loadBalancePolicy) {
        if (Objects.isNull(policy)) {
            policy = loadBalancePolicy;
        }
    }

    public static Instance balance(@NotNull List<Instance> instances) throws TrouveException {
        Instance instance = policy.select(instances);
        if (Objects.isNull(instance)) {
            throw new TrouveException("instance is null!");
        }
        return instance;
    }
}
