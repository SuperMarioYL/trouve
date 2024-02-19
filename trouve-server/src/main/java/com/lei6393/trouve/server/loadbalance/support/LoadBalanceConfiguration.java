package com.lei6393.trouve.server.loadbalance.support;

import com.lei6393.trouve.server.loadbalance.policy.RandomLoadBalancePolicy;
import com.lei6393.trouve.server.loadbalance.policy.ShortestResponseLoadBalancePolicy;
import com.lei6393.trouve.server.loadbalance.policy.WeightedRandomLoadBalancePolicy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yulei
 * @date 2022/6/8 12:22
 */
@Configuration
public class LoadBalanceConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RandomLoadBalancePolicy getRandomLoadBalancePolicy() {
        return new RandomLoadBalancePolicy();
    }

    @Bean
    @ConditionalOnMissingBean
    public WeightedRandomLoadBalancePolicy getWeightedRandomLoadBalancePolicy() {
        return new WeightedRandomLoadBalancePolicy();
    }

    @Bean
    @ConditionalOnMissingBean
    public ShortestResponseLoadBalancePolicy getShortestResponseLoadBalancePolicy() {
        return new ShortestResponseLoadBalancePolicy();
    }

    @Bean
    @ConditionalOnMissingBean
    public DispatchResponseTimeWindow getDispatchResponseTimeWindow() {
        return new DispatchResponseTimeWindow();
    }
}
