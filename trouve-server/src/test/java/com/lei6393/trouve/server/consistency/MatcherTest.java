package com.lei6393.trouve.server.consistency;

import com.lei6393.trouve.core.data.instance.Instance;
import com.lei6393.trouve.core.exception.TrouveUnregisteredUrlException;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * {@link Matcher} 读路径 null 守护与匹配回归测试（v1.2）。
 *
 * @author trouve
 */
public class MatcherTest {

    @Before
    public void reset() {
        // Matcher 持全局静态状态，每个用例前重置，避免相互污染
        Matcher.replace(new HashMap<>());
    }

    private Instance instance(String id, String ip, int port) {
        Instance instance = new Instance();
        instance.setInstanceId(id);
        instance.setIp(ip);
        instance.setPort(port);
        return instance;
    }

    @Test
    public void getInstances_miss_returnsEmptyNotNpe() {
        // 未注册 uri：返回空 List 而非 NPE（修复前 new ArrayList<>(null) 会抛 NPE）
        List<Instance> result = Matcher.getInstances("/not/registered");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void register_then_exactMatch() throws Exception {
        Matcher.register("/api/foo", instance("a", "127.0.0.1", 8080));
        assertEquals("/api/foo", Matcher.getBastMatchUri("/api/foo"));
        List<Instance> result = Matcher.getInstances("/api/foo");
        assertEquals(1, result.size());
        assertEquals("a", result.get(0).getInstanceId());
    }

    @Test
    public void register_patternMatch() throws Exception {
        Matcher.register("/api/**", instance("a", "127.0.0.1", 8080));
        String match = Matcher.getBastMatchUri("/api/foo/bar");
        assertEquals("/api/**", match);
        assertEquals(1, Matcher.getInstances(match).size());
    }

    @Test(expected = TrouveUnregisteredUrlException.class)
    public void unmatched_throwsUnregistered() throws Exception {
        Matcher.register("/api/foo", instance("a", "127.0.0.1", 8080));
        Matcher.getBastMatchUri("/totally/different");
    }

    @Test
    public void remove_unknownPattern_noNpe() {
        // 删除不存在的 pattern：no-op，不抛 NPE（修复前 get(pattern).removeIf 会抛 NPE）
        Matcher.remove("/never/registered", instance("x", "1.1.1.1", 1));
    }

    @Test
    public void remove_instance_keepsOthers() {
        Matcher.register("/api/foo", instance("a", "127.0.0.1", 8080));
        Matcher.register("/api/foo", instance("b", "127.0.0.1", 8081));
        Matcher.remove("/api/foo", instance("a", "127.0.0.1", 8080));
        List<Instance> result = Matcher.getInstances("/api/foo");
        assertEquals(1, result.size());
        assertEquals("b", result.get(0).getInstanceId());
    }
}
