package com.lei6393.trouve.server.instence;

import com.lei6393.trouve.core.data.instance.Instance;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * @author yulei
 * @date 2022/5/23 10:16
 */
public class InstanceHandlerRegistry {

    private static Collection<InstanceExtensionHandler> handlers;

    public static void register(Collection<InstanceExtensionHandler> beans) {
        handlers = beans;
    }

    public static void configExtensionInfoFromRequest(HttpServletRequest request) {
        for (InstanceExtensionHandler handler : handlers) {
            handler.configExtensionInfoFromRequest(request);
        }
    }

    public static void handleExtensionInfo(Instance needHandleInstance) {
        for (InstanceExtensionHandler handler : handlers) {
            handler.handleExtensionInfo(needHandleInstance);
        }
    }
}
