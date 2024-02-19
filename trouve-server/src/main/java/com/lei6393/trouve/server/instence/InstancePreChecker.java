package com.lei6393.trouve.server.instence;

import com.lei6393.trouve.core.data.instance.Instance;

import javax.validation.constraints.NotNull;

/**
 * 实例预检查
 */
public interface InstancePreChecker {

    boolean preCheck(@NotNull Instance instance);
}
