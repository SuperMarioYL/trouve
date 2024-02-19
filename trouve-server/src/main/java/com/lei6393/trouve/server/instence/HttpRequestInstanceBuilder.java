package com.lei6393.trouve.server.instence;

import com.lei6393.trouve.core.Constants;
import com.lei6393.trouve.core.data.instance.Instance;
import com.lei6393.trouve.core.data.instance.InstanceBuilder;
import com.lei6393.trouve.core.exception.TrouveException;
import com.lei6393.trouve.core.utils.WebUtil;
import com.lei6393.trouve.server.instence.generator.DefaultInstanceIdGenerator;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;

/**
 * @author leiyu
 * @date 2022/5/23 16:20
 */
public class HttpRequestInstanceBuilder {

    private final InstanceBuilder actualBuilder;

    public HttpRequestInstanceBuilder() {
        this.actualBuilder = InstanceBuilder.newBuilder();
    }

    public static HttpRequestInstanceBuilder newBuilder() {
        return new HttpRequestInstanceBuilder();
    }

    public HttpRequestInstanceBuilder setRequest(HttpServletRequest request) throws TrouveException {
        InstanceHandlerRegistry.configExtensionInfoFromRequest(request);
        setAttributesToBuilder(request);
        return this;
    }

    private void setAttributesToBuilder(HttpServletRequest request) throws TrouveException {
        actualBuilder.setServiceName(WebUtil.required(request, Instance.Param.SERVICE_NAME));
        actualBuilder.setIp(WebUtil.required(request, Instance.Param.IP));
        actualBuilder.setPort(Integer.parseInt(WebUtil.required(request, Instance.Param.PORT)));
        actualBuilder.setHealthy(BooleanUtils.toBoolean(WebUtil.optional(request, Instance.Param.HEALTHY, "true")));
        setWeight(request);
        setEnabled(request);
        setMetadata(request);
    }

    private void setWeight(HttpServletRequest request) throws TrouveException {
        int weight = Integer.parseInt(WebUtil.optional(request, Instance.Param.WEIGHT, "1"));
        if (weight > Constants.MAX_WEIGHT_VALUE || weight < Constants.MIN_WEIGHT_VALUE) {
            throw new TrouveException(MessageFormat.format(
                    "instance format invalid: The weights range from {0} to {1}",
                    Constants.MIN_WEIGHT_VALUE,
                    Constants.MAX_WEIGHT_VALUE)
            );
        }
        actualBuilder.setWeight(weight);
    }

    private void setEnabled(HttpServletRequest request) {
        String enabledString = WebUtil.optional(request, Instance.Param.ENABLED, StringUtils.EMPTY);
        boolean enabled;
        if (StringUtils.isBlank(enabledString)) {
            enabled = BooleanUtils.toBoolean(WebUtil.optional(request, Instance.Param.ENABLED, "true"));
        } else {
            enabled = BooleanUtils.toBoolean(enabledString);
        }
        actualBuilder.setEnabled(enabled);
    }

    private void setMetadata(HttpServletRequest request) throws TrouveException {
        String metadata = WebUtil.optional(request, Instance.Param.METADATA, StringUtils.EMPTY);
        if (StringUtils.isNotEmpty(metadata)) {
            actualBuilder.setMetadata(WebUtil.parseMetadata(metadata));
        }
    }

    /**
     * Build a new {@link Instance} and chain handled by {@link InstanceExtensionHandler}.
     *
     * @return new instance
     */
    public Instance build() {
        Instance result = actualBuilder.build();
        setInstanceId(result);
        InstanceHandlerRegistry.handleExtensionInfo(result);
        return result;
    }

    private void setInstanceId(Instance instance) {
        DefaultInstanceIdGenerator instanceIdGenerator = new DefaultInstanceIdGenerator(
                instance.getServiceName(), instance.getIp(), instance.getPort()
        );
        instance.setInstanceId(instanceIdGenerator.generateInstanceId());
    }
}
