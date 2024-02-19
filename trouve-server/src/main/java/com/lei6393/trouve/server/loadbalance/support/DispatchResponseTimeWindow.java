package com.lei6393.trouve.server.loadbalance.support;

import com.lei6393.trouve.core.data.instance.Instance;
import com.lei6393.trouve.server.bean.MatchPackage;
import com.lei6393.trouve.server.bean.request.RequestParam;
import com.lei6393.trouve.server.bean.response.ResponseParam;
import com.lei6393.trouve.server.dispatch.IDispatchInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.server.ServletServerHttpRequest;

/**
 * @author yulei
 * @date 2022/6/8 17:13
 */
public class DispatchResponseTimeWindow implements IDispatchInterceptor {


    @Override
    public void preMatch(@NotNull MatchPackage matchPackage) {
        // ignore
    }


    @Override
    public void preProcess(@NotNull RequestParam requestParam,
                           @NotNull ServletServerHttpRequest httpRequest,
                           @NotNull Instance instance) {
        // ignore
    }

    /**
     * 修改返回结果
     *
     * @param requestParam
     * @param instance
     * @param responseParam
     */
    @Override
    public void postProcess(@NotNull RequestParam requestParam,
                            @NotNull Instance instance,
                            long startTime,
                            long endTime,
                            @NotNull ResponseParam responseParam) {
        long duration = responseParam.getDuration();

    }

    /**
     * 执行异常逻辑
     *
     * @param requestParam
     * @param instance
     * @param startTime
     * @param endTime
     * @param throwable
     * @throws Throwable
     */
    @Override
    public void onProcessError(@NotNull RequestParam requestParam,
                               @NotNull Instance instance,
                               long startTime,
                               long endTime,
                               @NotNull Throwable throwable,
                               ResponseParam responseParam) throws Throwable {

    }
}
