package com.lei6393.trouve.server.instence;

import com.lei6393.trouve.core.data.instance.Instance;
import com.lei6393.trouve.core.exception.TrouveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * @author yulei
 * @date 2022/5/23 21:00
 */
public class InstanceOperatorRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceOperatorRegistry.class);

    private static Collection<InstanceOperator> operators;

    public static void register(Collection<InstanceOperator> beans) {
        operators = beans;
    }

    public static void registerInstance(String serviceName, Instance instance) throws TrouveException {
        for (InstanceOperator operator : operators) {
            operator.registerInstance(serviceName, instance);
        }
    }


    public static void removeInstance(String serviceName, Instance instance) throws TrouveException {
        for (InstanceOperator operator : operators) {
            operator.removeInstance(serviceName, instance);
        }
    }

    public static void updateInstance(String serviceName, Instance instance) throws TrouveException {
        for (InstanceOperator operator : operators) {
            operator.updateInstance(serviceName, instance);
        }
    }


}
