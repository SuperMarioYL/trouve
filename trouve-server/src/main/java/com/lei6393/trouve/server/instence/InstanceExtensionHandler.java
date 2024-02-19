package com.lei6393.trouve.server.instence;

import com.lei6393.trouve.core.data.instance.Instance;

import javax.servlet.http.HttpServletRequest;

/**
 * @author leiyu
 * @date 2022/5/23 10:01
 */
public interface InstanceExtensionHandler {

    /**
     * Config extension info from http request.
     *
     * @param request http request
     */
    void configExtensionInfoFromRequest(HttpServletRequest request);

    /**
     * Do handle for instance.
     *
     * @param needHandleInstance instance needed to be handled.
     */
    void handleExtensionInfo(Instance needHandleInstance);
}
