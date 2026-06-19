package com.lei6393.trouve.server.consistency;

import com.lei6393.trouve.core.data.instance.Instance;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;

/**
 * {@link Matcher} 并发安全压测（v1.3）。
 * <p>
 * 多读线程 + 注册/删除/replace 写线程并发运行一段时间，断言全程零异常
 * （修复前 HashMap + 可变 HashSet 会抛 ConcurrentModificationException / NPE）。
 *
 * @author trouve
 */
public class MatcherConcurrencyTest {

    @Before
    public void reset() {
        Matcher.replace(new HashMap<>());
    }

    private Instance instance(String id) {
        Instance instance = new Instance();
        instance.setInstanceId(id);
        instance.setIp("127.0.0.1");
        instance.setPort(8080);
        return instance;
    }

    @Test
    public void concurrentReadWrite_noException() throws Exception {
        final int routes = 32;
        for (int i = 0; i < routes; i++) {
            Matcher.register("/api/" + i + "/**", instance("seed-" + i));
        }

        final AtomicBoolean running = new AtomicBoolean(true);
        final ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();
        final int readers = 6;
        final int writers = 3;
        final CountDownLatch done = new CountDownLatch(readers + writers);

        for (int r = 0; r < readers; r++) {
            new Thread(() -> {
                try {
                    while (running.get()) {
                        for (int i = 0; i < routes; i++) {
                            Matcher.getInstances("/api/" + i + "/**");
                            try {
                                Matcher.getBastMatchUri("/api/" + i + "/foo/bar");
                            } catch (Exception ignore) {
                                // 未注册抛 TrouveUnregisteredUrlException 属正常业务异常，不计入
                            }
                        }
                    }
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    done.countDown();
                }
            }, "reader-" + r).start();
        }

        for (int w = 0; w < writers; w++) {
            final int base = w;
            new Thread(() -> {
                try {
                    int n = 0;
                    while (running.get()) {
                        String uri = "/api/" + (base * 11 + (n % routes)) + "/**";
                        Instance ins = instance("w" + base + "-" + n);
                        Matcher.register(uri, ins);
                        Matcher.remove(uri, ins);
                        if (n % 50 == 0) {
                            Map<String, Set<Instance>> snapshot = new HashMap<>();
                            Set<Instance> set = new HashSet<>();
                            set.add(instance("snap-" + n));
                            snapshot.put("/api/replaced/**", set);
                            Matcher.replace(snapshot);
                            // 重新铺一些路由，给读线程稳定的命中目标
                            for (int i = 0; i < routes; i++) {
                                Matcher.register("/api/" + i + "/**", instance("seed-" + i));
                            }
                        }
                        n++;
                    }
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    done.countDown();
                }
            }, "writer-" + w).start();
        }

        TimeUnit.MILLISECONDS.sleep(1500);
        running.set(false);
        assertTrue("threads did not finish in time", done.await(10, TimeUnit.SECONDS));

        if (!errors.isEmpty()) {
            throw new AssertionError("concurrent access raised " + errors.size()
                    + " errors, first: " + errors.peek(), errors.peek());
        }
    }
}
