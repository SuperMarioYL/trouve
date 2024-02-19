package com.lei6393.trouve.core.data.instance;

import com.lei6393.trouve.core.Constants;
import com.lei6393.trouve.core.utils.GsonUtil;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author leiyu
 * @date 2022/5/20 15:00
 */
public class Instance implements Serializable {

    private static final long serialVersionUID = -89798234234234243L;

    /**
     * unique id of this instance.
     */
    protected String instanceId;

    /**
     * instance ip.
     */
    protected String ip;

    /**
     * instance port.
     */
    protected int port;

    /**
     * instance weight.
     */
    protected int weight = Constants.DEFAULT_INSTANCE_WEIGHT;

    /**
     * instance health status.
     */
    protected boolean healthy = true;

    /**
     * If instance is enabled to accept request.
     */
    protected boolean enabled = true;

    /**
     * Service information of instance.
     */
    protected String serviceName;

    /**
     * Group information of instance.
     */
    protected String groupName;

    /**
     * user extended attributes.
     */
    protected Map<String, String> metadata = new HashMap<>();

    public Map<String, String> mapping() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(Param.INSTANCE_ID, instanceId);
        map.put(Param.IP, String.valueOf(ip));
        map.put(Param.PORT, String.valueOf(port));
        map.put(Param.WEIGHT, String.valueOf(weight));
        map.put(Param.HEALTHY, String.valueOf(healthy));
        map.put(Param.ENABLED, String.valueOf(enabled));
        map.put(Param.SERVICE_NAME, serviceName);
        map.put(Param.GROUP_NAME, groupName);
        map.put(Param.METADATA, GsonUtil.INSTANCE.toJson(metadata));
        return map;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public String getMetadataElement(String dataKey) {
        return metadata.get(dataKey);
    }

    public void addMetadataElement(String dataKey, String value) {
        metadata.put(dataKey, value);
    }

    public static class Param {

        public static final String INSTANCE_ID = "instance_id";

        public static final String IP = "ip";

        public static final String PORT = "port";

        public static final String WEIGHT = "weight";

        public static final String HEALTHY = "healthy";

        public static final String ENABLED = "enabled";

        public static final String SERVICE_NAME = "service_name";

        public static final String GROUP_NAME = "group_name";

        public static final String METADATA = "metadata";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Instance)) {
            return false;
        }

        Instance instance = (Instance) o;

        return new EqualsBuilder()
                .append(getPort(), instance.getPort())
                .append(getInstanceId(), instance.getInstanceId())
                .append(getIp(), instance.getIp())
                .append(getServiceName(), instance.getServiceName())
                .append(getGroupName(), instance.getGroupName())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getInstanceId())
                .append(getIp())
                .append(getPort())
                .append(getServiceName())
                .append(getGroupName())
                .toHashCode();
    }
}
