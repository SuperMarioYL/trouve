package com.lei6393.trouve.server.instence.generator;

/**
 * @author yulei
 * @date 2022/5/23 16:27
 */
public class DefaultInstanceIdGenerator implements IdGenerator{

    public static final String ID_DELIMITER = "#";

    private final String serviceName;

    private final String ip;

    private final int port;

    public DefaultInstanceIdGenerator(String serviceName, String ip, int port) {
        this.serviceName = serviceName;
        this.ip = ip;
        this.port = port;
    }

    /**
     * Generate instance id.
     *
     * @return instance id
     */
    @Override
    public String generateInstanceId() {
        return ip + ID_DELIMITER + port + ID_DELIMITER + serviceName;
    }
}
