package com.lei6393.trouve.server.common;

import com.lei6393.trouve.server.consistency.nonsingleton.redis.RedisConsistencyConfiguration;
import com.lei6393.trouve.server.consistency.singleton.SingletonConsistencyConfiguration;

/**
 * @author leiyu
 * @date 2022/5/26 17:33
 */
public class EnvProperties {

    /**
     * 开启 Redis 一致性存储，如果为 false 则启用 singleton 模式
     * {@link RedisConsistencyConfiguration}
     * {@link SingletonConsistencyConfiguration}
     */
    public static final String TROUVE_SERVER_REDIS_ENABLE = "trouve.server.redis.enable";

    /**
     * redis 地址 {@link org.redisson.config.SingleServerConfig}
     * <p>
     * Demo {@code 127.0.0.1:6379}
     */
    public static final String TROUVE_SERVER_REDIS_SINGLE_SERVER = "trouve.server.redis.singleServer";

    /**
     * redis 密码 {@link org.redisson.config.SingleServerConfig}
     */
    public static final String TROUVE_SERVER_REDIS_PASSWORD = "trouve.server.redis.password";


}
