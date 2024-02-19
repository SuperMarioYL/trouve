package com.lei6393.trouve.server.consistency.nonsingleton.redis;

import com.lei6393.trouve.core.MetaConstants;
import com.lei6393.trouve.core.data.MetaMsg;
import com.lei6393.trouve.core.data.RequestInfo;
import com.lei6393.trouve.core.data.instance.Instance;
import com.lei6393.trouve.core.utils.GsonUtil;
import com.lei6393.trouve.server.TrouveLoader;
import com.lei6393.trouve.server.consistency.AbstractHealthChecker;
import com.lei6393.trouve.server.consistency.AbstractMatcherUpdator;
import com.lei6393.trouve.server.consistency.Matcher;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.collections4.MapUtils;
import org.redisson.api.RBatch;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RMapAsync;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author yulei
 * @date 2022/5/25 11:14
 */
public class RedisMatcherUpdator extends AbstractMatcherUpdator {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisMatcherUpdator.class);

    private static final String META_URI_HASH_KEY = "meta_uri_hash_key_";

    private final RedissonClient rClient;

    public RedisMatcherUpdator(RedissonClient rClient, AbstractHealthChecker healthChecker, long flushDuration) {
        super(healthChecker, flushDuration);
        this.rClient = rClient;
    }

    @Override
    public void flushMatcher() throws Exception {
        RMap<String, Set<Instance>> rMap = rClient.getMap(getKey());
        Map<String, Set<Instance>> newMapping = rMap.readAllMap();
        if (MapUtils.isEmpty(newMapping)) {
            return;
        }

        boolean needFlush = false;
        RBatch batch = rClient.createBatch();
        RMapAsync<String, Set<Instance>> rMapAsync = batch.getMap(getKey());
        for (Map.Entry<String, Set<Instance>> entry : newMapping.entrySet()) {
            Set<Instance> instances = entry.getValue();
            boolean remove = getHealthChecker().filterHealthInstance(instances);
            needFlush |= remove;
            if (remove) {
                rMapAsync.fastPutAsync(entry.getKey(), instances);
            }
        }
        if (needFlush) {
            RLock rLock = rClient.getLock(META_URI_HASH_KEY + "lock");
            if (rLock.tryLock(0, 10, TimeUnit.SECONDS)) {
                batch.executeAsync();
            }
        } else {
            batch.discardAsync();
        }

        Matcher.replace(newMapping);
    }

    @Override
    public void registerMeta(MetaMsg meta) {
        RMap<String, Set<Instance>> rMap = rClient.getMap(getKey());

        Instance instance = meta.getInstance();
        String uriString = meta.getMetadataElement(MetaConstants.URI_INFOS);
        Set<RequestInfo> requestInfos = GsonUtil.INSTANCE.fromJson(uriString, new TypeToken<Set<RequestInfo>>() {
        }.getType());

        for (RequestInfo info : requestInfos) {
            for (String uri : info.getPatterns()) {
                Set<Instance> instances = rMap.get(uri);
                if (Objects.isNull(instances)) {
                    instances = new HashSet<>();
                }
                instances.add(instance);
                rMap.put(uri, instances);
            }
        }
    }

    @Override
    public void updateMeta(MetaMsg meta) {
        RMap<String, Set<Instance>> rMap = rClient.getMap(getKey());
        Instance instance = meta.getInstance();
        String uriString = meta.getMetadataElement(MetaConstants.URI_INFOS);
        Set<RequestInfo> requestInfos = GsonUtil.INSTANCE.fromJson(uriString, new TypeToken<Set<RequestInfo>>() {
        }.getType());

        for (RequestInfo info : requestInfos) {
            for (String uri : info.getPatterns()) {
                Set<Instance> instances = rMap.get(uri);
                if (Objects.isNull(instances)) {
                    instances = new HashSet<>();
                }
                instances.add(instance);
                rMap.put(uri, instances);
            }
        }
    }

    @Override
    public void removeMeta(MetaMsg meta) {
        // ignore
    }

    private static String getKey() {
        return META_URI_HASH_KEY + TrouveLoader.getNamespace();
    }
}
