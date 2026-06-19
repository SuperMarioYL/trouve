package com.lei6393.trouve.server.dispatch;

import com.lei6393.trouve.core.data.instance.Instance;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * 重试故障转移的候选选择回归测试（v2.0）。
 * <p>
 * 验证 {@link AbstractDispatchCenterProcessor#remainingInstances(List, Set)} 能正确排除已失败实例，
 * 从而保证重试切换到不同实例而非重锤同一个。
 *
 * @author trouve
 */
public class DispatchFailoverTest {

    private Instance instance(String id) {
        Instance instance = new Instance();
        instance.setInstanceId(id);
        instance.setIp("127.0.0.1");
        instance.setPort(8080);
        instance.setServiceName("svc");
        return instance;
    }

    @Test
    public void remaining_excludesTried() {
        Instance a = instance("a");
        Instance b = instance("b");
        Instance c = instance("c");
        List<Instance> candidates = Arrays.asList(a, b, c);

        Set<Instance> tried = new HashSet<>();
        tried.add(a);

        List<Instance> remaining = AbstractDispatchCenterProcessor.remainingInstances(candidates, tried);
        assertEquals(2, remaining.size());
        assertTrue(remaining.contains(b));
        assertTrue(remaining.contains(c));
    }

    @Test
    public void remaining_allTried_isEmpty() {
        Instance a = instance("a");
        Instance b = instance("b");
        List<Instance> candidates = Arrays.asList(a, b);

        Set<Instance> tried = new HashSet<>(candidates);

        List<Instance> remaining = AbstractDispatchCenterProcessor.remainingInstances(candidates, tried);
        assertTrue(remaining.isEmpty());
    }

    @Test
    public void remaining_noneTried_returnsAll() {
        List<Instance> candidates = new ArrayList<>();
        candidates.add(instance("a"));
        candidates.add(instance("b"));

        List<Instance> remaining =
                AbstractDispatchCenterProcessor.remainingInstances(candidates, new HashSet<>());
        assertEquals(2, remaining.size());
    }
}
