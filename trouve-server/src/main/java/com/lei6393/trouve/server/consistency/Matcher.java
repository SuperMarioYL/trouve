package com.lei6393.trouve.server.consistency;

import com.lei6393.trouve.core.data.instance.Instance;
import com.lei6393.trouve.core.exception.TrouveException;
import com.lei6393.trouve.core.exception.TrouveUnregisteredUrlException;
import com.lei6393.trouve.server.bean.MatchPackage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * url-instance 匹配器。
 * <p>
 * v1.3 并发与热路径改造：
 * <ul>
 *     <li>{@code uriMapping} 改为 {@link ConcurrentHashMap} + {@link ConcurrentHashMap#newKeySet()} 并发集合，
 *     注册/刷新线程写与请求线程读不再有数据竞争（消除 ConcurrentModificationException / HashMap 扩容损坏）；</li>
 *     <li>{@link #replace(Map)} 构建不可变快照后原子切换引用，读侧 {@link #getMatchInstance(MatchPackage)}
 *     单次捕获快照，避免读到半切换状态；</li>
 *     <li>缓存 {@link PatternsRequestCondition}，仅在路由 key 集合变化时重建，
 *     不再每请求 {@code keySet().toArray()} + new PatternsRequestCondition()；精确命中走 HashMap 快路径。</li>
 * </ul>
 *
 * @author leiyu
 * @date 2022/5/25 20:27
 */
public class Matcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Matcher.class);

    private static volatile Map<String, Set<Instance>> uriMapping = new ConcurrentHashMap<>();

    /**
     * 基于当前路由 key 集合预构建的匹配条件，避免每请求重建。
     */
    private static volatile PatternsRequestCondition patternCondition = new PatternsRequestCondition();

    public static List<Instance> getMatchInstance(@NotNull MatchPackage matchPackage) throws TrouveException {
        // 单次捕获快照，保证整个匹配过程看到一致的路由视图
        Map<String, Set<Instance>> mapping = uriMapping;
        PatternsRequestCondition condition = patternCondition;

        String match;
        if (StringUtils.isNotBlank(matchPackage.getLookupPath())) {
            match = matchUri(mapping, condition, matchPackage.getLookupPath());
        } else {
            match = matchUri(condition, matchPackage.getRequest());
        }
        return toInstances(mapping.get(match));
    }

    public static void register(@NotNull String pattern, @NotNull Set<Instance> instances) {
        boolean[] created = {false};
        Set<Instance> readyInstances = uriMapping.computeIfAbsent(pattern, key -> {
            created[0] = true;
            return ConcurrentHashMap.newKeySet();
        });
        readyInstances.addAll(instances);
        if (created[0]) {
            rebuildCondition();
        }
    }

    public static void register(@NotNull String pattern, @NotNull Instance instance) {
        boolean[] created = {false};
        Set<Instance> readyInstances = uriMapping.computeIfAbsent(pattern, key -> {
            created[0] = true;
            return ConcurrentHashMap.newKeySet();
        });
        readyInstances.add(instance);
        if (created[0]) {
            rebuildCondition();
        }
    }

    public static void remove(@NotNull Instance instance) {
        for (Set<Instance> instances : uriMapping.values()) {
            instances.removeIf(ready -> ready.equals(instance));
        }
    }

    public static void remove(@NotNull String pattern, @NotNull Instance instance) {
        Set<Instance> instances = uriMapping.get(pattern);
        if (Objects.nonNull(instances)) {
            instances.removeIf(ready -> ready.equals(instance));
        }
    }

    public static void remove(@NotNull AbstractHealthChecker healthChecker) {
        for (Set<Instance> instances : uriMapping.values()) {
            healthChecker.filterHealthInstance(instances);
        }
    }

    public static void replace(@NotNull Map<String, Set<Instance>> newUriMapping) {
        // 拷贝为并发结构的不可变快照，再原子切换引用
        Map<String, Set<Instance>> snapshot = new ConcurrentHashMap<>();
        for (Map.Entry<String, Set<Instance>> entry : newUriMapping.entrySet()) {
            Set<Instance> set = ConcurrentHashMap.newKeySet();
            if (Objects.nonNull(entry.getValue())) {
                set.addAll(entry.getValue());
            }
            snapshot.put(entry.getKey(), set);
        }
        uriMapping = snapshot;
        rebuildConditionFrom(snapshot);
    }

    public static String getBastMatchUri(@NotNull String pattern) throws TrouveUnregisteredUrlException {
        return matchUri(uriMapping, patternCondition, pattern);
    }

    public static String getBastMatchUri(@NotNull HttpServletRequest request) throws TrouveUnregisteredUrlException {
        return matchUri(patternCondition, request);
    }

    public static List<Instance> getInstances(String uri) {
        return toInstances(uriMapping.get(uri));
    }

    // ----------------------------- internals -----------------------------

    private static String matchUri(Map<String, Set<Instance>> mapping,
                                   PatternsRequestCondition condition,
                                   String pattern) throws TrouveUnregisteredUrlException {
        // 精确命中快路径，避免 Ant 模式解析开销
        if (mapping.containsKey(pattern)) {
            return pattern;
        }
        List<String> patterns = condition.getMatchingPatterns(pattern);
        if (CollectionUtils.isNotEmpty(patterns)) {
            return patterns.get(0);
        }
        throw new TrouveUnregisteredUrlException();
    }

    private static String matchUri(PatternsRequestCondition condition,
                                   HttpServletRequest request) throws TrouveUnregisteredUrlException {
        PatternsRequestCondition matched = condition.getMatchingCondition(request);
        if (Objects.nonNull(matched)) {
            String[] patterns = matched.getPatterns().toArray(new String[]{});
            if (patterns.length > 0) {
                return patterns[0];
            }
        }
        throw new TrouveUnregisteredUrlException();
    }

    private static List<Instance> toInstances(Set<Instance> instances) {
        if (CollectionUtils.isEmpty(instances)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(instances);
    }

    private static void rebuildCondition() {
        rebuildConditionFrom(uriMapping);
    }

    private static void rebuildConditionFrom(Map<String, Set<Instance>> mapping) {
        patternCondition = new PatternsRequestCondition(mapping.keySet().toArray(new String[]{}));
    }
}
