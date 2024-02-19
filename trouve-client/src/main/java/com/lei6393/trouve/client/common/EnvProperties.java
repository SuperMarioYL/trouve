package com.lei6393.trouve.client.common;

import java.net.InetAddress;

/**
 * 可配置的环境变量
 *
 * @author yulei
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

}
