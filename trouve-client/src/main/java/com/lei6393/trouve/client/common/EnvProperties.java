package com.lei6393.trouve.client.common;

import java.net.InetAddress;

/**
 * 可配置的环境变量
 *
 * @author leiyu
 * @date 2022/5/26 16:15
 */
public class EnvProperties {

    /**
     * 优先获取的对外暴露的 port
     */
    public static final String TROUVE_CLIENT_PORT_PROPERTY = "trouve.client.port";


    /**
     * 如果 "trouve.client.port" 为空，则获取该值作为对外暴露的 port
     */
    public static final String SERVER_PORT_PROPERTY = "server.port";

    /**
     * 优先获取的对外暴露的 IP，如果为空值，则取 {@link InetAddress#getLocalHost()}
     */
    public static final String TROUVE_CLIENT_IP_PROPERTY = "trouve.client.ip";

    /**
     * 优先获取的 trouve 服务端地址，支持传多个值，用","分割
     */
    public static final String TROUVE_SERVER_ADDRESS = "trouve.server.address";

    /**
     * 控制面鉴权令牌，需与服务端 {@code trouve.server.token} 一致；留空则不携带令牌。
     */
    public static final String TROUVE_CLIENT_TOKEN = "trouve.client.token";

    /**
     * 服务名（starter 自动装配使用）。设置该属性即可启用注册，无需 {@code @EnableTrouveRegistry}。
     */
    public static final String TROUVE_CLIENT_SERVICE_NAME = "trouve.client.service-name";

    /**
     * 心跳间隔（毫秒），starter 可选配置，默认 5000。
     */
    public static final String TROUVE_CLIENT_HEART_RATE_INTERVAL = "trouve.client.heart-rate-interval";

    /**
     * meta 更新间隔（毫秒），starter 可选配置，默认 600000。
     */
    public static final String TROUVE_CLIENT_META_UPDATE_INTERVAL = "trouve.client.meta-update-interval";

}
