package com.lei6393.trouve.client.api;

import com.lei6393.trouve.core.data.RequestInfo;
import org.springframework.context.ApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author yulei
 * @date 2022/5/13 10:58
 */
public class ExposeApiRegistry {

    private static final Map<RequestMappingInfo, HandlerMethod> MAPPING = new HashMap<>();

    public static Map<RequestMappingInfo, HandlerMethod> getApiMapping() {
        return MAPPING;
    }

    public static Set<RequestMappingInfo> getRequestMapping() {
        return new HashSet<>(MAPPING.keySet());
    }

    public static Set<RequestInfo> getRequestInfoSet() {
        Set<RequestInfo> infos = new HashSet<>();
        for (RequestMappingInfo mappingInfo : MAPPING.keySet()) {
            infos.add(RequestInfo.of(mappingInfo));
        }
        return infos;
    }

    public static void registerURIMapping(ApplicationContext context) {
        RequestMappingHandlerMapping handlerMapping =
                (RequestMappingHandlerMapping) context.getBean("requestMappingHandlerMapping");
        Map<RequestMappingInfo, HandlerMethod> map = handlerMapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : map.entrySet()) {
            HandlerMethod method = entry.getValue();
            ExposeApi api = method.getMethodAnnotation(ExposeApi.class);
            if (Objects.isNull(api)) {
                api = method.getBeanType().getAnnotation(ExposeApi.class);
            }
            if (Objects.nonNull(api)) {
                MAPPING.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
