package com.lei6393.trouve.server.consistency;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link AbstractHealthChecker} fail-open 修复回归测试（v1.3）。
 * <p>
 * 修复前：healthInstanceIds 为空集合时 checkHealth 返回 true（把所有实例当健康）。
 * 修复后：一旦完成首次刷新（已初始化），空集合表示"无健康实例"，应对所有实例返回 false。
 *
 * @author trouve
 */
public class AbstractHealthCheckerTest {

    /**
     * 可控健康集合的测试实现，period 设为 1 小时避免自动刷新干扰，手动调用 run() 驱动状态。
     */
    static class TestChecker extends AbstractHealthChecker {
        volatile Set<String> next = new HashSet<>();

        TestChecker() {
            super(3_600_000L, 1000L);
        }

        @Override
        public Set<String> flushHealthInstanceIds() {
            return next;
        }
    }

    @Test
    public void initializedEmpty_treatsAllUnhealthy() {
        TestChecker checker = new TestChecker();
        checker.next = new HashSet<>();
        checker.run(); // 已初始化为空集合
        // 关键断言：修复后空健康集合 => 无人健康（修复前会返回 true）
        assertFalse(checker.checkHealth("any-instance"));
    }

    @Test
    public void initializedWithIds_membership() {
        TestChecker checker = new TestChecker();
        checker.next = new HashSet<>(Arrays.asList("a", "b"));
        checker.run();
        assertTrue(checker.checkHealth("a"));
        assertTrue(checker.checkHealth("b"));
        assertFalse(checker.checkHealth("c"));
    }

    @Test
    public void nullInstanceId_unhealthy() {
        TestChecker checker = new TestChecker();
        assertFalse(checker.checkHealth((String) null));
    }
}
