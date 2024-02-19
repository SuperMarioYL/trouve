package com.lei6393.trouve.client;

import com.lei6393.trouve.client.data.instance.DefaultInstanceFactory;
import com.lei6393.trouve.client.data.instance.InstanceFactory;
import com.lei6393.trouve.client.data.meta.DefaultMetaFactory;
import com.lei6393.trouve.client.data.meta.MetaFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author leiyu
 * @date 2022/5/16 10:12
 */
@Configuration
public class DefaultTrouveRegistryConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public InstanceFactory getInstanceFactory() {
        return new DefaultInstanceFactory();
    }


    @Bean
    @ConditionalOnMissingBean
    public MetaFactory getMetaFactory() {
        return new DefaultMetaFactory();
    }

}
