package com.lei6393.trouve.core.data.instance;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author leiyu
 * @date 2022/5/23 13:51
 */
public class InstanceBuilder {

    private String instanceId;

    private String ip;

    private Integer port;

    private int weight;

    private Boolean healthy;

    private Boolean enabled;

    private String serviceName;

    private Map<String, String> metadata = new HashMap<>();

    private InstanceBuilder() {
    }

    public InstanceBuilder setInstanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    public InstanceBuilder setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public InstanceBuilder setPort(Integer port) {
        this.port = port;
        return this;
    }

    public InstanceBuilder setWeight(int weight) {
        this.weight = weight;
        return this;
    }

    public InstanceBuilder setHealthy(Boolean healthy) {
        this.healthy = healthy;
        return this;
    }

    public InstanceBuilder setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public InstanceBuilder setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public InstanceBuilder setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public InstanceBuilder addMetadata(String metaKey, String metaValue) {
        this.metadata.put(metaKey, metaValue);
        return this;
    }

    /**
     * Build a new {@link Instance}.
     *
     * @return new instance
     */
    public Instance build() {
        Instance result = new Instance();
        if (!Objects.isNull(instanceId)) {
            result.setInstanceId(instanceId);
        }
        if (!Objects.isNull(ip)) {
            result.setIp(ip);
        }
        if (!Objects.isNull(port)) {
            result.setPort(port);
        }
        if (!Objects.isNull(weight)) {
            result.setWeight(weight);
        }
        if (!Objects.isNull(healthy)) {
            result.setHealthy(healthy);
        }
        if (!Objects.isNull(enabled)) {
            result.setEnabled(enabled);
        }
        if (!Objects.isNull(serviceName)) {
            result.setServiceName(serviceName);
        }
        result.setMetadata(metadata);
        return result;
    }

    public static InstanceBuilder newBuilder() {
        return new InstanceBuilder();
    }
}
