package com.lei6393.trouve.server;

import com.lei6393.trouve.server.controller.InstanceController;
import com.lei6393.trouve.server.controller.TrouveEntranceController;
import com.lei6393.trouve.server.loadbalance.policy.WeightedRandomLoadBalancePolicy;
import org.junit.After;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * spring-boot-starter 自动装配集成测试（v2.2）。
 * <p>
 * 仅通过属性 {@code trouve.server.namespace}（不使用 {@code @EnableTrouveDiscover} 注解）启动上下文，
 * 验证 properties-only 装配路径真正生效：namespace 被加载、核心 bean 被创建。
 *
 * @author trouve
 */
public class TrouveServerAutoConfigurationTest {

    @After
    public void tearDown() {
        // TrouveLoader 为单应用 set-once 静态状态，多上下文测试间需重置
        TrouveLoader.resetForTest();
    }

    @Test
    public void propertiesOnly_autoConfigures() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        Map<String, Object> props = new HashMap<>();
        props.put("trouve.server.namespace", "openapi");
        props.put("trouve.server.redis.enable", "false");
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", props));

        context.register(TrouveServerAutoConfiguration.class);
        context.refresh();

        try {
            // properties-only 路径加载了 namespace（无注解）
            assertEquals("openapi", TrouveLoader.getNamespace());
            // 自动装配创建了控制面与负载均衡 bean
            assertNotNull(context.getBean(InstanceController.class));
            assertNotNull(context.getBean(WeightedRandomLoadBalancePolicy.class));
            // auto-entrance 未开启 -> 不注册内置入口控制器
            assertFalse(context.getBeanNamesForType(TrouveEntranceController.class).length > 0);
        } finally {
            context.close();
        }
    }

    @Test
    public void autoEntrance_optIn_registersController() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        Map<String, Object> props = new HashMap<>();
        props.put("trouve.server.namespace", "openapi");
        props.put("trouve.server.redis.enable", "false");
        props.put("trouve.server.auto-entrance", "true");
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", props));

        context.register(TrouveServerAutoConfiguration.class);
        context.refresh();

        try {
            assertTrue(context.getBeanNamesForType(TrouveEntranceController.class).length > 0);
        } finally {
            context.close();
        }
    }
}
