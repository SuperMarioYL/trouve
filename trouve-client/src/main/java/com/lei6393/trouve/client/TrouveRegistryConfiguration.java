package com.lei6393.trouve.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yulei
 * @date 2022/5/16 10:12
 */
@Configuration
public class TrouveRegistryConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TrouveRegistry getServiceRegistry() {
        return new TrouveRegistry();
    }

}
