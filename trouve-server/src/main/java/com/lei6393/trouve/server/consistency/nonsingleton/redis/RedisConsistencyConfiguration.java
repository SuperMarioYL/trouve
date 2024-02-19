package com.lei6393.trouve.server.consistency.nonsingleton.redis;

import com.lei6393.trouve.server.common.Constants;
import com.lei6393.trouve.server.common.EnvProperties;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.SerializationCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author leiyu
 * @date 2022/5/25 10:02
 */
@Configuration
@ConditionalOnProperty(name = EnvProperties.TROUVE_SERVER_REDIS_ENABLE, havingValue = "true")
@ConfigurationProperties(prefix = "trouve.server.redis")
public class RedisConsistencyConfiguration {

    String singleServer;

    String password;

    @Bean("trouve_r_client")
    public RedissonClient getRClient() {
        if (StringUtils.isBlank(singleServer)) {
            throw new RuntimeException("trouve redis single server is null");
        }

        Config config = new Config();
        config.setCodec(new SerializationCodec());

        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setAddress(Constants.REDIS_SCHEMA + singleServer);

        if (StringUtils.isNoneBlank(password)) {
            singleServerConfig.setPassword(password);
        }

        return Redisson.create(config);
    }

    @Bean("redis_instance_health_checker")
    public RedisInstanceHealthChecker getRedisInstanceHealthChecker(@Qualifier("trouve_r_client") RedissonClient rClient) {
        return new RedisInstanceHealthChecker(rClient, 10000, 30000);
    }

    @Bean("redis_uri_updator")
    public RedisMatcherUpdator getRedisUriCenter(@Qualifier("trouve_r_client") RedissonClient rClient,
                                                 @Qualifier("redis_instance_health_checker")
                                                 RedisInstanceHealthChecker checker) {
        return new RedisMatcherUpdator(rClient, checker, 10000);
    }

    public String getSingleServer() {
        return singleServer;
    }

    public void setSingleServer(String singleServer) {
        this.singleServer = singleServer;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
