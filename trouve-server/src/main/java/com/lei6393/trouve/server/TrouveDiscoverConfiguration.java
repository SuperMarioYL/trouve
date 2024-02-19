package com.lei6393.trouve.server;

import com.lei6393.trouve.server.controller.InstanceController;
import com.lei6393.trouve.server.controller.MetaController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yulei
 * @date 2022/5/19 00:01
 */
@Configuration
public class TrouveDiscoverConfiguration {


    @Bean(value = "trouve_instance_controller", autowireCandidate = false)
    @ConditionalOnMissingBean
    public InstanceController getInstanceController() {
        return new InstanceController();
    }

    @Bean(value = "trouve_meta_controller", autowireCandidate = false)
    @ConditionalOnMissingBean
    public MetaController getMetaController() {
        return new MetaController();
    }

    @Bean("trouve_loader")
    @ConditionalOnMissingBean
    public TrouveLoader getTrouveLoader() {
        return new TrouveLoader();
    }

}
