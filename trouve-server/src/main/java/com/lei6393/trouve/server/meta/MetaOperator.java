package com.lei6393.trouve.server.meta;

import com.lei6393.trouve.core.data.MetaMsg;

/**
 * @author yulei
 * @date 2022/5/25 12:02
 */
public interface MetaOperator {

    void registerMeta(MetaMsg meta);

    void updateMeta(MetaMsg meta);

    void removeMeta(MetaMsg meta);
}
