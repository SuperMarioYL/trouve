package com.lei6393.trouve.server.instence;

import com.lei6393.trouve.core.data.instance.Instance;
import com.lei6393.trouve.core.exception.TrouveException;

/**
 * @author yulei
 * @date 2022/5/23 14:34
 */
public interface InstanceOperator {

    /**
     * Register an instance to a service in AP mode.
     *
     * @param serviceName grouped service name group@@service
     * @param instance    instance to register
     * @throws TrouveException trouve exception when register failed
     */
    void registerInstance(String serviceName, Instance instance) throws TrouveException;

    /**
     * Remove instance from service.
     *
     * @param serviceName grouped service name group@@service
     * @param instance    instance
     * @throws TrouveException trouve exception when remove failed
     */
    void removeInstance(String serviceName, Instance instance) throws TrouveException;

    /**
     * Update instance information. Due to the basic information can't be changed, so this update should only update
     * metadata.
     *
     * <p>Update API will replace the whole metadata with new input instance.
     *
     * @param serviceName grouped service name group@@service
     * @param instance    instance
     * @throws TrouveException trouve exception when update failed
     */
    void updateInstance(String serviceName, Instance instance) throws TrouveException;

}
