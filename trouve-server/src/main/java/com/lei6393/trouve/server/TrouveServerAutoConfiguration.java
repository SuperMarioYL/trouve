package com.lei6393.trouve.server;

import com.lei6393.trouve.server.common.EnvProperties;
import com.lei6393.trouve.server.consistency.nonsingleton.redis.RedisConsistencyConfiguration;
import com.lei6393.trouve.server.consistency.singleton.SingletonConsistencyConfiguration;
import com.lei6393.trouve.server.controller.TrouveEntranceController;
import com.lei6393.trouve.server.loadbalance.support.LoadBalanceConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Trouve server 端 spring-boot-starter 自动装配。
 * <p>
 * 设置 {@code trouve.server.namespace} 即可启用，无需 {@code @EnableTrouveDiscover} 注解。
 * 装配的配置与注解 {@code @Import} 的一致，命名空间 / 分发配置 / 负载均衡策略由
 * {@link TrouveLoader} 的 properties-only 路径以默认值加载。
 *
 * @author trouve
 */
@AutoConfiguration
@ConditionalOnProperty(name = EnvProperties.TROUVE_SERVER_NAMESPACE)
@Import({
        TrouveDiscoverConfiguration.class,
        SingletonConsistencyConfiguration.class,
        RedisConsistencyConfiguration.class,
        LoadBalanceConfiguration.class
})
public class TrouveServerAutoConfiguration {

    /**
     * 可选自动注册内置转发入口控制器，需 {@code trouve.server.auto-entrance=true}。
     */
    @Bean
    @ConditionalOnProperty(name = EnvProperties.TROUVE_SERVER_AUTO_ENTRANCE, havingValue = "true")
    @ConditionalOnMissingBean
    public TrouveEntranceController trouveEntranceController() {
        return new TrouveEntranceController();
    }
}
