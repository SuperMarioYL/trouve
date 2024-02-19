package com.lei6393.trouve.client.data.instance;

import com.lei6393.trouve.client.EnableTrouveRegistry;
import com.lei6393.trouve.client.utils.TrouveEnvUtil;
import com.lei6393.trouve.core.data.instance.Instance;
import com.lei6393.trouve.core.data.instance.InstanceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Map;
import java.util.Objects;

/**
 * @author leiyu
 * @date 2022/5/23 15:15
 */
public class DefaultInstanceFactory implements InstanceFactory, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultInstanceFactory.class);

    private String serviceName;

    @Override
    public Instance create() {
        return InstanceBuilder.newBuilder()
                .setPort(TrouveEnvUtil.getPort())
                .setIp(TrouveEnvUtil.getSelfIP())
                .setServiceName(getServiceName())
                .build();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(EnableTrouveRegistry.class);

        for (Object bean : beanMap.values()) {
            EnableTrouveRegistry enableTrouveRegistry =
                    AnnotationUtils.findAnnotation(bean.getClass(), EnableTrouveRegistry.class);
            if (Objects.nonNull(enableTrouveRegistry)) {
                this.serviceName = enableTrouveRegistry.value();
                break;
            }
        }
    }

    public String getServiceName() {
        return serviceName;
    }
}
