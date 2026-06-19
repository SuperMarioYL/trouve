package com.lei6393.trouve.server.dispatch.health;

import com.lei6393.trouve.core.data.instance.Instance;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * {@link ActiveHealthRegistry} 过滤与开关回归测试（v2.x）。
 *
 * @author trouve
 */
public class ActiveHealthRegistryTest {

    @After
    public void tearDown() {
        ActiveHealthRegistry.configure(false, 3, 2);
        ActiveHealthRegistry.resetForTest();
    }

    private Instance instance(String id) {
        Instance instance = new Instance();
        instance.setInstanceId(id);
        instance.setIp("127.0.0.1");
        instance.setPort(8080);
        return instance;
    }

    @Test
    public void disabled_filterIsNoop() {
        ActiveHealthRegistry.configure(false, 3, 2);
        List<Instance> in = new ArrayList<>();
        in.add(instance("a"));
        assertEquals(1, ActiveHealthRegistry.filterHealthy(in).size());
        assertTrue(ActiveHealthRegistry.isHealthy("anything"));
    }

    @Test
    public void enabled_removesUnhealthy() {
        ActiveHealthRegistry.configure(true, 2, 2);
        Instance a = instance("a");
        Instance b = instance("b");
        // 把 a 探活打成不健康（连续 2 次失败）
        ActiveHealthRegistry.recordFailure("a");
        ActiveHealthRegistry.recordFailure("a");

        List<Instance> candidates = new ArrayList<>();
        candidates.add(a);
        candidates.add(b);

        List<Instance> healthy = ActiveHealthRegistry.filterHealthy(candidates);
        assertEquals(1, healthy.size());
        assertEquals("b", healthy.get(0).getInstanceId());
    }

    @Test
    public void enabled_allUnhealthy_failOpen() {
        ActiveHealthRegistry.configure(true, 1, 2);
        Instance a = instance("a");
        ActiveHealthRegistry.recordFailure("a"); // 阈值 1 -> 不健康

        List<Instance> candidates = new ArrayList<>();
        candidates.add(a);
        // 全部不健康 -> 回退原列表（fail-open）
        assertEquals(1, ActiveHealthRegistry.filterHealthy(candidates).size());
    }
}
