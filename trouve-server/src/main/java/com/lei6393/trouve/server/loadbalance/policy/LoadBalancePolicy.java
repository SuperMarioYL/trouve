package com.lei6393.trouve.server.loadbalance.policy;


import com.lei6393.trouve.core.Trouve;
import com.lei6393.trouve.core.data.instance.Instance;
import com.lei6393.trouve.server.EnableTrouveDiscover;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 负载均衡策略需要实现的接口，可自行实现，实现方式：
 * 1. 实现该接口。
 * 2. 实现类通过 {@link Trouve @trouve} 进行注入。
 * 3. 实现类在 {@link EnableTrouveDiscover#dispatchLoadBalancePolicy()} 进行指定。
 *
 * 实现类例子:
 * <pre>
 *     {@code
 *
 * @Trouve
 * public class RandomLoadBalancePolicy extends AbstractLoadBalancePolicy {
 *
 *     @Override
 *     public Instance doSelect(@NotNull List<Instance> instances) {
 *         int length = instances.size();
 *         return instances.get(ThreadLocalRandom.current().nextInt(length));
 *     }
 * }
 *
 *     }
 * </pre>
 *
 * @author yulei
 * @date 2022/6/7 20:15
 */
public interface LoadBalancePolicy {


    /**
     * 运用负载均衡策略挑选实例
     *
     * @param instances
     * @return
     */
    Instance select(@NotNull List<Instance> instances);
}
