package com.lei6393.trouve.server;

import com.lei6393.trouve.core.event.EventListenerHolder;
import com.lei6393.trouve.core.event.IEventListener;
import com.lei6393.trouve.core.utils.EnvUtil;
import com.lei6393.trouve.server.common.DispatchHttpProperty;
import com.lei6393.trouve.server.dispatch.DispatchInterceptorRegistry;
import com.lei6393.trouve.server.dispatch.DispatchNetworkHelper;
import com.lei6393.trouve.server.dispatch.IDispatchInterceptor;
import com.lei6393.trouve.server.instence.InstanceExtensionHandler;
import com.lei6393.trouve.server.instence.InstanceHandlerRegistry;
import com.lei6393.trouve.server.instence.InstanceOperator;
import com.lei6393.trouve.server.instence.InstanceOperatorRegistry;
import com.lei6393.trouve.server.instence.InstancePreChecker;
import com.lei6393.trouve.server.instence.InstancePreCheckerRegistry;
import com.lei6393.trouve.server.loadbalance.Balancer;
import com.lei6393.trouve.server.loadbalance.policy.LoadBalancePolicy;
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        loadContext(applicationContext);
        loadBasicInformationFromAnnotation(applicationContext);

        // 环境变量配置
        EnvUtil.setEnvironment((ConfigurableEnvironment) getContext().getEnvironment());

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

    private static void loadBasicInformationFromAnnotation(ApplicationContext context) {
        Map<String, Object> beanMap = context.getBeansWithAnnotation(EnableTrouveDiscover.class);

        for (Object bean : beanMap.values()) {
            EnableTrouveDiscover trouveDiscover = AnnotationUtils
                    .findAnnotation(bean.getClass(), EnableTrouveDiscover.class);

            if (Objects.nonNull(trouveDiscover)) {
                loadTrouveDiscover(trouveDiscover);
                break;
            }
        }

    }

    private static void loadTrouveDiscover(EnableTrouveDiscover discover) {
        namespace = discover.value();
        DispatchHttpProperty httpProperty = discover.dispatchHttpProperty();
        DispatchNetworkHelper.loadProperty(httpProperty);
        loadBalancePolicyFromAnnotation(discover);
    }

    private static void loadBalancePolicyFromAnnotation(EnableTrouveDiscover discover) {
        Class<? extends LoadBalancePolicy> clazz = discover.dispatchLoadBalancePolicy();
        LoadBalancePolicy policy = getContext().getBean(clazz);
        Balancer.defineLoadBalancePolicy(policy);
    }
}
