package com.lei6393.trouve.server.consistency;

import com.lei6393.trouve.core.data.instance.Instance;
import com.lei6393.trouve.core.exception.TrouveException;
import com.lei6393.trouve.core.exception.TrouveUnregisteredUrlException;
import com.lei6393.trouve.server.bean.MatchPackage;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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

/**
 * url-instance 匹配器
 *
 * @author leiyu
 * @date 2022/5/25 20:27
 */
public class Matcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Matcher.class);

    private static volatile Map<String, Set<Instance>> uriMapping = Maps.newHashMap();

    public static List<Instance> getMatchInstance(@NotNull MatchPackage matchPackage) throws TrouveException {
        String match;
        if (StringUtils.isNotBlank(matchPackage.getLookupPath())) {
            match = getBastMatchUri(matchPackage.getLookupPath());
        } else {
            match = getBastMatchUri(matchPackage.getRequest());
        }
        return getInstances(match);
    }

    public static void register(@NotNull String pattern, @NotNull Set<Instance> instances) {
        Set<Instance> readyInstances = uriMapping.get(pattern);
        if (Objects.isNull(readyInstances)) {
            uriMapping.put(pattern, instances);
        } else {
            readyInstances.addAll(instances);
        }
    }

    public static void register(@NotNull String pattern, @NotNull Instance instance) {
        Set<Instance> readyInstances = uriMapping.get(pattern);
        if (Objects.isNull(readyInstances)) {
            uriMapping.put(pattern, Sets.newHashSet(instance));
        } else {
            readyInstances.add(instance);
        }
    }

    public static void remove(@NotNull Instance instance) {
        for (Set<Instance> instances : uriMapping.values()) {
            instances.removeIf(ready -> ready.equals(instance));
        }
    }

    public static void remove(@NotNull String pattern, @NotNull Instance instance) {
        Set<Instance> instances = uriMapping.get(pattern);
        instances.removeIf(ready -> ready.equals(instance));
    }


    public static void remove(@NotNull AbstractHealthChecker healthChecker) {
        for (Set<Instance> instances : uriMapping.values()) {
            healthChecker.filterHealthInstance(instances);
        }
    }

    public static void replace(@NotNull Map<String, Set<Instance>> newUriMapping) {
        uriMapping = newUriMapping;
    }

    public static String getBastMatchUri(@NotNull String pattern) throws TrouveUnregisteredUrlException {
        if (uriMapping.containsKey(pattern)) {
            return pattern;
        }
        String[] paths = uriMapping.keySet().toArray(new String[]{});
        List<String> patterns = new PatternsRequestCondition(paths).getMatchingPatterns(pattern);
        String match = null;
        if (CollectionUtils.isNotEmpty(patterns)) {
            match = patterns.get(0);
        } else {
            throw new TrouveUnregisteredUrlException();
        }
        return match;
    }

    public static String getBastMatchUri(@NotNull HttpServletRequest request) throws TrouveUnregisteredUrlException {
        String[] paths = uriMapping.keySet().toArray(new String[]{});
        PatternsRequestCondition condition = new PatternsRequestCondition(paths).getMatchingCondition(request);
        String match = null;
        if (Objects.nonNull(condition)) {
            match = condition.getPatterns().toArray(new String[]{})[0];
        } else {
            throw new TrouveUnregisteredUrlException();
        }
        return match;
    }

    public static List<Instance> getInstances(String uri) {
        return new ArrayList<>(uriMapping.get(uri));
    }

}
