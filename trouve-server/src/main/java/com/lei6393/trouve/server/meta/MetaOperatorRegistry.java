package com.lei6393.trouve.server.meta;

import com.lei6393.trouve.core.data.MetaMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * @author leiyu
 * @date 2022/5/25 12:04
 */
public class MetaOperatorRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetaOperator.class);

    private static Collection<MetaOperator> operators;

    public static void register(Collection<MetaOperator> beans) {
        operators = beans;
    }

    public static void registerMeta(MetaMsg meta) {
        for (MetaOperator operator : operators) {
            operator.registerMeta(meta);
        }
    }

    public static void updateMeta(MetaMsg meta) {
        for (MetaOperator operator : operators) {
            operator.updateMeta(meta);
        }
    }

    public static void removeMeta(MetaMsg meta) {
        for (MetaOperator operator : operators) {
            operator.removeMeta(meta);
        }
    }


}
