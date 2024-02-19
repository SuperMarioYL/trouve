package com.lei6393.trouve.server.consistency.singleton;

import com.lei6393.trouve.core.MetaConstants;
import com.lei6393.trouve.core.data.MetaMsg;
import com.lei6393.trouve.core.data.RequestInfo;
import com.lei6393.trouve.core.data.instance.Instance;
import com.lei6393.trouve.core.utils.GsonUtil;
import com.lei6393.trouve.server.consistency.AbstractHealthChecker;
import com.lei6393.trouve.server.consistency.AbstractMatcherUpdator;
import com.lei6393.trouve.server.consistency.Matcher;
import com.google.gson.reflect.TypeToken;

import java.util.Set;

/**
 * @author leiyu
 * @date 2022/5/23 22:37
 */
public class SingletonMatcherUpdator extends AbstractMatcherUpdator {

    public SingletonMatcherUpdator(AbstractHealthChecker healthChecker, long flushDuration) {
        super(healthChecker, flushDuration);
    }

    @Override
    public void flushMatcher() throws Exception {
        Matcher.remove(getHealthChecker());
    }

    @Override
    public void registerMeta(MetaMsg meta) {
        Instance instance = meta.getInstance();
        String uriString = meta.getMetadataElement(MetaConstants.URI_INFOS);
        Set<RequestInfo> requestInfos = GsonUtil.INSTANCE.fromJson(uriString, new TypeToken<Set<RequestInfo>>() {
        }.getType());

        for (RequestInfo info : requestInfos) {
            for (String uri : info.getPatterns()) {
                Matcher.register(uri, instance);
            }
        }
    }

    @Override
    public void updateMeta(MetaMsg meta) {
        Instance instance = meta.getInstance();
        String uriString = meta.getMetadataElement(MetaConstants.URI_INFOS);
        Set<RequestInfo> requestInfos = GsonUtil.INSTANCE.fromJson(uriString, new TypeToken<Set<RequestInfo>>() {
        }.getType());

        for (RequestInfo info : requestInfos) {
            for (String uri : info.getPatterns()) {
                Matcher.register(uri, instance);
            }
        }
    }

    @Override
    public void removeMeta(MetaMsg meta) {
        Instance instance = meta.getInstance();
        String uriString = meta.getMetadataElement(MetaConstants.URI_INFOS);
        Set<RequestInfo> requestInfos = GsonUtil.INSTANCE.fromJson(uriString, new TypeToken<Set<RequestInfo>>() {
        }.getType());

        for (RequestInfo info : requestInfos) {
            for (String uri : info.getPatterns()) {
                Matcher.remove(uri, instance);
            }
        }
    }
}
