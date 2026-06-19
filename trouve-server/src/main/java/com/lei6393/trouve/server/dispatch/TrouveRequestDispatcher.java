package com.lei6393.trouve.server.dispatch;

import com.lei6393.trouve.core.data.instance.Instance;
import com.lei6393.trouve.core.exception.TrouveEmptyInstanceException;
import com.lei6393.trouve.core.exception.TrouveErrorType;
import com.lei6393.trouve.core.exception.TrouveException;
import com.lei6393.trouve.core.exception.TrouvePayloadTooLargeException;
import com.lei6393.trouve.core.exception.TrouveUnregisteredUrlException;
import com.lei6393.trouve.server.bean.MatchPackage;
import com.lei6393.trouve.server.bean.request.RequestParam;
import com.lei6393.trouve.server.bean.response.ResponseParam;
import com.lei6393.trouve.server.dispatch.metrics.DispatchMetrics;
import com.lei6393.trouve.server.dispatch.resilience.ConcurrencyLimiter;
import com.lei6393.trouve.server.loadbalance.Balancer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServletServerHttpResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Trouve 请求分发入口，使用方式：
 * <pre>
 * {@code
 * @RestController
 * public class EntranceController {
 *
 *     @RequestMapping("/**")
 *     public void entrance(HttpServletRequest request, HttpServletResponse response) {
 *         try {
 *             TrouveRequestDispatcher.entrance(request, response);
 *         } catch (Throwable e) {
 *             LOGGER.error("entrance error!", e);
 *         }
 *     }
 *
 * }
 * }
 * </pre>
 *
 * @author leiyu
 * @date 2022/5/25 22:08
 */
public class TrouveRequestDispatcher extends AbstractDispatchCenterProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrouveRequestDispatcher.class);

    /**
     * 入口
     *
     * @param request  源请求
     * @param response 源返回值
     * @throws Throwable
     */
    public static void entrance(@NotNull HttpServletRequest request,
                                @NotNull HttpServletResponse response) throws Throwable {
        try (ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response)) {
            // 优雅停机：drain 期间拒绝新请求，等待在途完成
            if (GatewayDrain.isDraining()) {
                flushError(httpResponse, HttpStatus.SERVICE_UNAVAILABLE,
                        TrouveErrorType.DEFAULT_ERROR.getCode(), "trouve gateway is draining");
                return;
            }
            // 入口并发限流：饱和时快速失败 503，避免无界盲转发耗尽线程
            if (!ConcurrencyLimiter.tryAcquire()) {
                DispatchMetrics.recordRejected();
                LOGGER.warn("gateway concurrency limit reached, reject request: {}", request.getRequestURI());
                flushError(httpResponse, HttpStatus.SERVICE_UNAVAILABLE,
                        TrouveErrorType.DEFAULT_ERROR.getCode(), "trouve gateway overloaded");
                return;
            }
            DispatchMetrics.recordRequest();
            GatewayDrain.enter();
            try {
                // 请求体体积守护（opt-in）：超过上限直接 413，缓解整体缓冲 OOM
                if (exceedsBodyLimit(request.getContentLengthLong(), maxBodyLimit())) {
                    throw new TrouvePayloadTooLargeException();
                }

                // 构建请求匹配包
                MatchPackage matchPackage = MatchPackage.create(request);

                // 匹配候选实例列表
                List<Instance> candidates = candidateInstances(matchPackage);

                // 负载均衡选出首选实例
                Instance instance = Balancer.balance(candidates);

                // 组装请求参数
                RequestParam requestParam = assembleRequestParam(request, matchPackage, instance);

                // 执行转发（失败时在候选间故障转移），并返回结果
                ResponseParam responseParam = execute(requestParam, instance, candidates);

                // 将结果填入 http response
                flush(httpResponse, responseParam);
            } catch (TrouvePayloadTooLargeException e) {
                // 请求体超限 -> 413
                LOGGER.warn("request body too large: {}", request.getRequestURI());
                flushError(httpResponse, HttpStatus.PAYLOAD_TOO_LARGE,
                        TrouveErrorType.PAYLOAD_TOO_LARGE_ERROR.getCode(), e.getMessage());
            } catch (TrouveUnregisteredUrlException e) {
                // 未注册路由 -> 404
                LOGGER.warn("no route matched: {}", e.getMessage());
                flushError(httpResponse, HttpStatus.NOT_FOUND,
                        TrouveErrorType.UNREGISTERED_URL_ERROR.getCode(), e.getMessage());
            } catch (TrouveEmptyInstanceException e) {
                // 无可用实例 -> 503
                LOGGER.warn("no available instance: {}", e.getMessage());
                flushError(httpResponse, HttpStatus.SERVICE_UNAVAILABLE,
                        TrouveErrorType.EMPTY_INSTANCE_ERROR.getCode(), e.getMessage());
            } catch (TrouveException e) {
                // 其它 trouve 内部错误 -> 500
                LOGGER.error("trouve dispatch error", e);
                flushError(httpResponse, HttpStatus.INTERNAL_SERVER_ERROR, e.getCode(), e.getMessage());
            } finally {
                GatewayDrain.exit();
                ConcurrencyLimiter.release();
            }
        }
    }

}
