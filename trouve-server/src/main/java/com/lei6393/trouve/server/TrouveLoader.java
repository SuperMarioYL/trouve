package com.lei6393.trouve.server;

import com.lei6393.trouve.core.event.EventListenerHolder;
import com.lei6393.trouve.core.event.IEventListener;
import com.lei6393.trouve.core.utils.EnvUtil;
import com.lei6393.trouve.server.auth.RegistryAuthenticator;
import com.lei6393.trouve.server.common.DispatchHttpProperty;
import com.lei6393.trouve.server.common.EnvProperties;
import com.lei6393.trouve.server.dispatch.DispatchInterceptorRegistry;
import com.lei6393.trouve.server.dispatch.DispatchNetworkHelper;
import com.lei6393.trouve.server.dispatch.IDispatchInterceptor;
import com.lei6393.trouve.server.dispatch.health.ActiveHealthProber;
import com.lei6393.trouve.server.dispatch.health.ActiveHealthRegistry;
import com.lei6393.trouve.server.dispatch.resilience.CircuitBreakerRegistry;
import com.lei6393.trouve.server.dispatch.resilience.ConcurrencyLimiter;
import com.lei6393.trouve.server.instence.InstanceExtensionHandler;
import com.lei6393.trouve.server.instence.InstanceHandlerRegistry;
import com.lei6393.trouve.server.instence.InstanceOperator;
import com.lei6393.trouve.server.instence.InstanceOperatorRegistry;
import com.lei6393.trouve.server.instence.InstancePreChecker;
import com.lei6393.trouve.server.instence.InstancePreCheckerRegistry;
import com.lei6393.trouve.server.loadbalance.Balancer;
import com.lei6393.trouve.server.loadbalance.policy.LoadBalancePolicy;
import com.lei6393.trouve.server.loadbalance.policy.WeightedRandomLoadBalancePolicy;
import com.lei6393.trouve.server.meta.MetaOperator;
import com.lei6393.trouve.server.meta.MetaOperatorRegistry;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * @author leiyu
 * @date 2022/5/23 10:07
 */
public class TrouveLoader implements ApplicationContextAware {

    private static ApplicationContext context;

    private static String namespace;

    private static ActiveHealthProber activeHealthProber;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        loadContext(applicationContext);

        // 环境变量配置（须先于读取基础信息，loadTrouveDiscover/loadFromProperties 依赖 EnvUtil）
        EnvUtil.setEnvironment((ConfigurableEnvironment) getContext().getEnvironment());

        loadBasicInformationFromAnnotation(applicationContext);

        // 事件监听器注册
        EventListenerHolder.registerListeners(getCollectionBeans(IEventListener.class));

        // 实例预查验器注册
        InstancePreCheckerRegistry.register(getCollectionBeans(InstancePreChecker.class));

        // 实例补充句柄注册
        InstanceHandlerRegistry.register(getCollectionBeans(InstanceExtensionHandler.class));

        // 实例操作器注册
        InstanceOperatorRegistry.register(getCollectionBeans(InstanceOperator.class));

        // 元信息操作器注册
        MetaOperatorRegistry.register(getCollectionBeans(MetaOperator.class));

        // 分发拦截器注册
        DispatchInterceptorRegistry.register(getCollectionBeans(IDispatchInterceptor.class));
    }

    public static ApplicationContext getContext() {
        return context;
    }

    public static String getNamespace() {
        return namespace;
    }

    public static <T> Collection<T> getCollectionBeans(@Nullable Class<T> type) {
        return getContext().getBeansOfType(type).values();
    }

    private static void loadContext(ApplicationContext applicationContext) {
        if (Objects.isNull(context)) {
            context = applicationContext;
        }
    }

    /**
     * 测试用：重置全局静态状态（context/namespace 为单应用 set-once 语义，多上下文测试需重置）。
     */
    static void resetForTest() {
        context = null;
        namespace = null;
    }

    private static void loadBasicInformationFromAnnotation(ApplicationContext context) {
        Map<String, Object> beanMap = context.getBeansWithAnnotation(EnableTrouveDiscover.class);

        for (Object bean : beanMap.values()) {
            EnableTrouveDiscover trouveDiscover = AnnotationUtils
                    .findAnnotation(bean.getClass(), EnableTrouveDiscover.class);

            if (Objects.nonNull(trouveDiscover)) {
                loadTrouveDiscover(trouveDiscover);
                return;
            }
        }

        // 无注解：走 spring-boot-starter 的 properties-only 装配路径
        loadFromProperties();
    }

    private static void loadTrouveDiscover(EnableTrouveDiscover discover) {
        namespace = discover.value();
        DispatchHttpProperty httpProperty = discover.dispatchHttpProperty();
        configureDispatch(httpProperty);
        loadBalancePolicy(discover.dispatchLoadBalancePolicy());
    }

    /**
     * properties-only 装配（starter）：namespace 取自 {@code trouve.server.namespace}，
     * 分发配置用内置默认值，负载均衡用默认加权随机策略。
     */
    private static void loadFromProperties() {
        namespace = EnvUtil.getEnv(EnvProperties.TROUVE_SERVER_NAMESPACE);
        if (namespace == null) {
            return;
        }
        configureDispatch(defaultDispatchProperty());
        loadBalancePolicy(WeightedRandomLoadBalancePolicy.class);
    }

    private static void configureDispatch(DispatchHttpProperty httpProperty) {
        DispatchNetworkHelper.loadProperty(httpProperty);
        CircuitBreakerRegistry.configure(
                httpProperty.circuitBreakerEnabled(),
                httpProperty.circuitBreakerFailureThreshold(),
                httpProperty.circuitBreakerOpenMillis());
        ConcurrencyLimiter.configure(httpProperty.maxConcurrentRequests());
        RegistryAuthenticator.configure(EnvUtil.getEnv(EnvProperties.TROUVE_SERVER_TOKEN));
        ActiveHealthRegistry.configure(
                httpProperty.activeHealthCheckEnabled(),
                httpProperty.activeHealthFailThreshold(),
                httpProperty.activeHealthRiseThreshold());
        if (httpProperty.activeHealthCheckEnabled() && activeHealthProber == null) {
            activeHealthProber = new ActiveHealthProber(
                    httpProperty.activeHealthCheckPath(),
                    httpProperty.activeHealthCheckIntervalMillis());
        }
    }

    private static void loadBalancePolicy(Class<? extends LoadBalancePolicy> clazz) {
        LoadBalancePolicy policy = getContext().getBean(clazz);
        Balancer.defineLoadBalancePolicy(policy);
    }

    /**
     * 合成一份全默认值的 {@link DispatchHttpProperty}（用于 properties-only 装配）。
     */
    private static DispatchHttpProperty defaultDispatchProperty() {
        return DefaultDispatchHolder.class.getAnnotation(DispatchHttpProperty.class);
    }

    @DispatchHttpProperty
    private static final class DefaultDispatchHolder {
    }
}
