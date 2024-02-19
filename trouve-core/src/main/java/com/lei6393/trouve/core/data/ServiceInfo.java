package com.lei6393.trouve.core.data;

import com.lei6393.trouve.core.MetaConstants;
import com.lei6393.trouve.core.data.instance.Instance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务信息
 *
 * @author yulei
 * @date 2022/5/20 19:46
 */
public class ServiceInfo implements Serializable {

    private String name;

    private String groupName;

    private List<Instance> instances = new ArrayList<>();

    private Map<String, Object> metaData = new HashMap<>();

    public ServiceInfo(String name, String groupName) {
        this.name = name;
        this.groupName = groupName;
    }

    public String getGroupedName() {
        return groupName + MetaConstants.SERVICE_INFO_SPLITER + name;
    }

    public void addMeta(String key, Object value) {
        this.metaData.put(key, value);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<Instance> getInstances() {
        return instances;
    }

    public void setInstances(List<Instance> instances) {
        this.instances = instances;
    }

    public Map<String, Object> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, Object> metaData) {
        this.metaData = metaData;
    }
}
