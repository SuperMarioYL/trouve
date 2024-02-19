package com.lei6393.trouve.server;

import com.lei6393.trouve.server.common.DispatchHttpProperty;
import com.lei6393.trouve.server.common.EnvProperties;
import com.lei6393.trouve.server.consistency.nonsingleton.redis.RedisConsistencyConfiguration;
import com.lei6393.trouve.server.consistency.singleton.SingletonConsistencyConfiguration;
import com.lei6393.trouve.server.loadbalance.policy.LoadBalancePolicy;
import com.lei6393.trouve.server.loadbalance.policy.ShortestResponseLoadBalancePolicy;
import com.lei6393.trouve.server.loadbalance.policy.WeightedRandomLoadBalancePolicy;
import com.lei6393.trouve.server.loadbalance.policy.RandomLoadBalancePolicy;
import com.lei6393.trouve.server.loadbalance.support.LoadBalanceConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Indexed;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开启服务发现
 * 相关可配置属性详见 {@link EnvProperties }
 *
 * @author yulei
 * @date 2022/5/18 23:43
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Indexed
@Import({
        TrouveDiscoverConfiguration.class,
        SingletonConsistencyConfiguration.class,
        RedisConsistencyConfiguration.class,
        LoadBalanceConfiguration.class
})
public @interface EnableTrouveDiscover {

    /**
     * namespace
     *
     * @return
     */
    String value();

    /**
     * 分发器 HTTP 设置
     *
     * @return
     */
    DispatchHttpProperty dispatchHttpProperty() default @DispatchHttpProperty;

    /**
     * 分发器负载均衡策略
     * <p>
     * 提供三种负载均衡策略：
     * <p>
     * 1. 加权随机负载均衡（默认策略）
     * <p>
     * {@link WeightedRandomLoadBalancePolicy WeightedRandomLoadBalancePolicy.class}
     * <p>
     * 2. 随机负载均衡
     * <p>
     * {@link RandomLoadBalancePolicy
     * RandomLoadBalancePolicy.class
     * }
     * <p>
     * 3. 最短响应负载均衡
     * <p>
     * {@link ShortestResponseLoadBalancePolicy
     * ShortestResponseLoadBalancePolicy.class
     * }
     * <p>
     * 如需扩展，需自行实现 {@link LoadBalancePolicy}
     *
     * @return 策略 class
     */
    Class<? extends LoadBalancePolicy> dispatchLoadBalancePolicy() default WeightedRandomLoadBalancePolicy.class;
}
