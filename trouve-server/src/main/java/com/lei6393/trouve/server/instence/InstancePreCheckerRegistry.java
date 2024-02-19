package com.lei6393.trouve.server.instence;

import com.lei6393.trouve.core.data.instance.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;

public class InstancePreCheckerRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstancePreCheckerRegistry.class);

    private static Collection<InstancePreChecker> checkers;


    public static void register(Collection<InstancePreChecker> instancePreCheckers) {
        checkers = instancePreCheckers;
    }

    public static boolean preChecker(Instance instance) {
        if (Objects.isNull(instance)) {
            return false;
        }
        boolean flag = true;
        for (InstancePreChecker checker : checkers) {
            flag &= checker.preCheck(instance);
        }
        return flag;
    }

}
