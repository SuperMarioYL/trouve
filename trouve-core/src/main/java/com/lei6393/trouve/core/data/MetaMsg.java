package com.lei6393.trouve.core.data;

import com.lei6393.trouve.core.data.instance.Instance;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yulei
 * @date 2022/5/24 23:50
 */
public class MetaMsg implements Serializable {

    private static final long serialVersionUID = -8974235465734243L;

    protected Instance instance;

    protected Map<String, String> metadata = new HashMap<>();

    public MetaMsg(Instance instance) {
        this.instance = instance;
    }

    public String getMetadataElement(String dataKey) {
        return metadata.get(dataKey);
    }

    public void addMetadataElement(String dataKey, String value) {
        metadata.put(dataKey, value);
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
