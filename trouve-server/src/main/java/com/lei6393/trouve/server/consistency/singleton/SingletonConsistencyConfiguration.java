package com.lei6393.trouve.server.consistency.singleton;

import com.lei6393.trouve.server.common.EnvProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author leiyu
 * @date 2022/5/25 09:54
 */
@Configuration
@ConditionalOnProperty(name = EnvProperties.TROUVE_SERVER_REDIS_ENABLE, havingValue = "false", matchIfMissing = true)
public class SingletonConsistencyConfiguration {


    @Bean("singleton_instance_health_checker")
    public SingletonInstanceHealthChecker getSingletonInstanceHealthChecker() {
        return new SingletonInstanceHealthChecker(10000, 30000);
    }

    @Bean("singleton_uri_updator")
    public SingletonMatcherUpdator getSingletonURIUpdator(
            @Qualifier("singleton_instance_health_checker") SingletonInstanceHealthChecker healthChecker) {
        return new SingletonMatcherUpdator(healthChecker, 10000);
    }

}
