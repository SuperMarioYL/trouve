package com.lei6393.trouve.server.dispatch;

import com.lei6393.trouve.core.data.instance.Instance;
import com.lei6393.trouve.server.bean.MatchPackage;
import com.lei6393.trouve.server.bean.request.RequestParam;
import com.lei6393.trouve.server.bean.response.ResponseParam;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServletServerHttpRequest;

import java.util.Collection;

/**
 * @author yulei
 * @date 2022/5/25 23:39
 */
public class DispatchInterceptorRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(DispatchInterceptorRegistry.class);

    private static Collection<IDispatchInterceptor> interceptors;

    public static void register(Collection<IDispatchInterceptor> beans) {
        interceptors = beans;
    }

    /**
     * 在匹配实例前可对匹配包进行修改
     *
     * @param matchPackage
     */
    public static void preMatch(@NotNull MatchPackage matchPackage) {
        for (IDispatchInterceptor interceptor : interceptors) {
            interceptor.preMatch(matchPackage);
        }
    }

    /**
     * 再转发前对请求包 requestParam 进行修改
     *
     * @param requestParam
     * @param httpRequest
     * @param instance
     */
    public static void preProcess(@NotNull RequestParam requestParam,
                                  @NotNull ServletServerHttpRequest httpRequest,
                                  @NotNull Instance instance) {
        for (IDispatchInterceptor interceptor : interceptors) {
            interceptor.preProcess(requestParam, httpRequest, instance);
        }
    }

    /**
     * 对结果进行修改
     *
     * @param responseParam
     */
    public static void postProcess(@NotNull RequestParam requestParam,
                                   @NotNull Instance instance,
                                   long startTime,
                                   long endTime,
                                   @NotNull ResponseParam responseParam) {
        for (IDispatchInterceptor interceptor : interceptors) {
            interceptor.postProcess(requestParam, instance, startTime, endTime, responseParam);
        }
    }

    public static void onProcessError(@NotNull RequestParam requestParam,
                                      @NotNull Instance instance,
                                      long startTime,
                                      long endTime,
                                      @NotNull Throwable throwable,
                                      ResponseParam responseParam) throws Throwable {
        for (IDispatchInterceptor interceptor : interceptors) {
            interceptor.onProcessError(requestParam, instance, startTime, endTime, throwable, responseParam);
        }
    }

}
