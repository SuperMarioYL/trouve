package com.lei6393.trouve.server.dispatch;

import com.lei6393.trouve.core.Trouve;
import com.lei6393.trouve.core.data.instance.Instance;
import com.lei6393.trouve.server.bean.MatchPackage;
import com.lei6393.trouve.server.bean.request.RequestParam;
import com.lei6393.trouve.server.bean.response.ResponseParam;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.server.ServletServerHttpRequest;

/**
 * 分发拦截器，使用 {@link Trouve @trouve} 进行注入，例如：
 * <pre>
 * {@code
 * @Trouve
 * public class DispatchInterceptor implements IDispatchInterceptor {
 *
 *     void preMatch(MatchPackage matchPackage) {
 *         // ignore
 *     }
 *
 *     void preProcess(RequestParam requestParam, ServletServerHttpRequest httpRequest, Instance instance) {
 *         // ignore
 *     }
 *
 *     void postProcess(ResponseParam responseParam) {
 *         // ignore
 *     }
 * }
 * }
 * </pre>
 *
 * @author yulei
 * @date 2022/5/25 22:28
 */
public interface IDispatchInterceptor {

    /**
     * 匹配前调用
     *
     * @param matchPackage
     */
    void preMatch(@NotNull MatchPackage matchPackage);

    /**
     * 将请求包进行修改
     *
     * @param requestParam
     * @param httpRequest
     * @param instance
     */
    void preProcess(@NotNull RequestParam requestParam,
                    @NotNull ServletServerHttpRequest httpRequest,
                    @NotNull Instance instance);

    /**
     * 执行后处理
     *
     * @param requestParam
     * @param instance
     * @param startTime
     * @param endTime
     * @param responseParam
     */
    void postProcess(@NotNull RequestParam requestParam,
                     @NotNull Instance instance,
                     long startTime,
                     long endTime,
                     @NotNull ResponseParam responseParam);

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
    void onProcessError(@NotNull RequestParam requestParam,
                        @NotNull Instance instance,
                        long startTime,
                        long endTime,
                        @NotNull Throwable throwable,
                        ResponseParam responseParam) throws Throwable;
}
