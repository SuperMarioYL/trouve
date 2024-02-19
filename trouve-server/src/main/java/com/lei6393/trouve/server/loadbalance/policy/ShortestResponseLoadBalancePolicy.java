package com.lei6393.trouve.server.loadbalance.policy;

import com.lei6393.trouve.core.data.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 最短响应负载均衡策略
 *
 * @author yulei
 * @date 2022/6/8 16:17
 */
public class ShortestResponseLoadBalancePolicy extends AbstractLoadBalancePolicy {


    @Override
    protected Instance doSelect(@NotNull List<Instance> instances) {
        return null;
    }
}
