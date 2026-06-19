package com.lei6393.trouve.client;

import com.lei6393.trouve.client.api.ExposeApiRegistry;
import com.lei6393.trouve.client.data.instance.InstanceFactory;
import com.lei6393.trouve.client.data.meta.MetaFactory;
import com.lei6393.trouve.core.utils.EnvUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;
import java.util.Objects;

/**
 * @author leiyu
 * @date 2022/5/11 20:27
 */
public class TrouveRegistry extends ScheduledRegistry implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrouveRegistry.class);

    public static String serviceName;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        LOGGER.info("service registry start!");

        // 环境变量配置
        EnvUtil.setEnvironment((ConfigurableEnvironment) applicationContext.getEnvironment());

        // 从注解加载信息；无注解则走 properties-only（starter）路径
        loadBasicInfoFromAnnotation(applicationContext);

        if (serviceName == null) {
            LOGGER.warn("trouve service name is not configured (no @EnableTrouveRegistry and no '{}'), skip registry.",
                    com.lei6393.trouve.client.common.EnvProperties.TROUVE_CLIENT_SERVICE_NAME);
            return;
        }

        // 对外暴露 API 注册
        ExposeApiRegistry.registerURIMapping(applicationContext);

        // 获取实例工厂, 如果有多个实例工厂，将抛出异常
        setFactory(applicationContext.getBean(InstanceFactory.class), applicationContext.getBean(MetaFactory.class));

        // 定时注册任务启动
        scheduleStart();

        LOGGER.info("service registry success!");
    }

    private void loadBasicInfoFromAnnotation(ApplicationContext applicationContext) {
        Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(EnableTrouveRegistry.class);
        for (Object bean : beanMap.values()) {
            EnableTrouveRegistry enableTrouveRegistry = AnnotationUtils
                    .findAnnotation(bean.getClass(), EnableTrouveRegistry.class);

            if (Objects.nonNull(enableTrouveRegistry)) {
                setHeartRateInterval(enableTrouveRegistry.heartRateInterval());
                setMetaUpdateInterval(enableTrouveRegistry.metaUpdateInterval());
                serviceName = enableTrouveRegistry.value();
                loadServerAddresses(enableTrouveRegistry);
                return;
            }
        }

        // 无注解：走 spring-boot-starter 的 properties-only 装配路径
        loadFromProperties();
    }

    /**
     * properties-only 装配（starter）：服务名取自 {@code trouve.client.service-name}，
     * 心跳 / meta 间隔取可选配置（默认 5000 / 600000），服务端地址取自 {@code trouve.server.address}。
     */
    private void loadFromProperties() {
        String name = EnvUtil.getEnv(com.lei6393.trouve.client.common.EnvProperties.TROUVE_CLIENT_SERVICE_NAME);
        if (name == null) {
            return;
        }
        serviceName = name;
        setHeartRateInterval(EnvUtil.getProperty(
                com.lei6393.trouve.client.common.EnvProperties.TROUVE_CLIENT_HEART_RATE_INTERVAL,
                Long.class, 5 * 1000L));
        setMetaUpdateInterval(EnvUtil.getProperty(
                com.lei6393.trouve.client.common.EnvProperties.TROUVE_CLIENT_META_UPDATE_INTERVAL,
                Long.class, 10 * 60 * 1000L));
        loadServerAddressesFromEnv();
    }
}
