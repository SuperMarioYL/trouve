package com.lei6393.trouve.server.common;

import com.lei6393.trouve.server.consistency.nonsingleton.redis.RedisConsistencyConfiguration;
import com.lei6393.trouve.server.consistency.singleton.SingletonConsistencyConfiguration;

/**
 * @author leiyu
 * @date 2022/5/26 17:33
 */
public class EnvProperties {

    /**
     * 服务发现命名空间（starter 自动装配使用）。设置该属性即可启用 trouve server，无需 {@code @EnableTrouveDiscover}。
     */
    public static final String TROUVE_SERVER_NAMESPACE = "trouve.server.namespace";

    /**
     * 是否自动注册内置 catch-all 转发入口控制器，默认 false（需手写 EntranceController，避免双重映射）。
     */
    public static final String TROUVE_SERVER_AUTO_ENTRANCE = "trouve.server.auto-entrance";

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

    /**
     * 控制面共享鉴权令牌。配置后，注册 / 心跳 / 元信息接口要求 client 携带匹配令牌；
     * 留空则不启用鉴权（与旧版兼容）。
     */
    public static final String TROUVE_SERVER_TOKEN = "trouve.server.token";

}
