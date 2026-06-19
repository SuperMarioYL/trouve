package com.lei6393.trouve.server.dispatch;

import com.lei6393.trouve.core.data.instance.Instance;
import com.lei6393.trouve.core.exception.TrouveEmptyInstanceException;
import com.lei6393.trouve.core.exception.TrouveException;
import com.lei6393.trouve.server.bean.MatchPackage;
import com.lei6393.trouve.server.bean.request.RequestParam;
import com.lei6393.trouve.server.bean.request.UrlParam;
import com.lei6393.trouve.server.bean.response.ResponseParam;
import com.lei6393.trouve.server.consistency.Matcher;
import com.lei6393.trouve.server.dispatch.health.ActiveHealthRegistry;
import com.lei6393.trouve.server.dispatch.metrics.DispatchMetrics;
import com.lei6393.trouve.server.dispatch.resilience.CircuitBreakerRegistry;
import com.lei6393.trouve.server.loadbalance.Balancer;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServletServerHttpResponse;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author leiyu
 * @date 2022/5/25 23:45
 */
public abstract class AbstractDispatchCenterProcessor extends DispatchComposeHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDispatchCenterProcessor.class);

    /**
     * 获取匹配到的候选实例列表（供负载均衡与故障转移选择）。
     *
     * @param matchPackage 请求匹配包
     * @return 非空候选实例列表
     * @throws TrouveException 未匹配到路由或无可用实例
     */
    protected static List<Instance> candidateInstances(MatchPackage matchPackage) throws TrouveException {
        DispatchInterceptorRegistry.preMatch(matchPackage);

        List<Instance> instances = Matcher.getMatchInstance(matchPackage);
        if (CollectionUtils.isEmpty(instances)) {
            throw new TrouveEmptyInstanceException();
        }
        // 依次摘除主动探活不健康与熔断（OPEN）的实例；任一过滤后为空则回退（fail-open）
        return ActiveHealthRegistry.filterHealthy(CircuitBreakerRegistry.filterAllowed(instances));
    }

    /**
     * 从候选中选取一个尚未尝试过的实例做故障转移；若都已尝试过，则回退到全量再选一个。
     *
     * @param candidates 候选实例
     * @param tried      已尝试过的实例
     * @return 选中的实例
     * @throws TrouveException 负载均衡失败
     */
    protected static Instance selectInstance(List<Instance> candidates, Set<Instance> tried) throws TrouveException {
        List<Instance> remaining = remainingInstances(candidates, tried);
        if (remaining.isEmpty()) {
            // 所有候选都失败过：退化为在全量里再选（退避后重试好过直接放弃）
            return Balancer.balance(candidates);
        }
        return Balancer.balance(remaining);
    }

    /**
     * 计算候选中尚未尝试过的实例（纯函数，便于测试）。
     */
    static List<Instance> remainingInstances(List<Instance> candidates, Set<Instance> tried) {
        List<Instance> remaining = new ArrayList<>();
        for (Instance candidate : candidates) {
            if (!tried.contains(candidate)) {
                remaining.add(candidate);
            }
        }
        return remaining;
    }

    /**
     * 执行分发调用。
     * <p>
     * 行为约定：
     * <ul>
     *     <li>总尝试次数 = 1 次首发 + max(retryCount, 0) 次重试（见 {@link #totalAttempts(int)}）；</li>
     *     <li>一旦拿到上游真实响应（即便是 4xx/5xx）立即返回，不再重复转发，避免非幂等请求副作用重复；</li>
     *     <li>所有尝试均因 IO 异常失败时，返回 502（连接失败）或 504（超时），<b>绝不</b>伪装成成功的空 200。</li>
     * </ul>
     * v2.0：重试不再重发同一实例，而是经 {@link #selectInstance(List, Set)} 故障转移到
     * 另一个尚未失败的健康实例。
     *
     * @param requestParam  请求参数包
     * @param firstInstance 首次目标实例（已由负载均衡选出）
     * @param candidates    全部候选实例（用于重试时故障转移）
     * @return 上游响应或网关错误响应
     * @throws Throwable 负载均衡失败或拦截器 {@code onProcessError} 可能重抛的异常
     */
    protected static ResponseParam execute(@NotNull RequestParam requestParam,
                                           @NotNull Instance firstInstance,
                                           @NotNull List<Instance> candidates) throws Throwable {
        // 总尝试次数 = 1 次首发 + N 次重试
        int maxAttempts = totalAttempts(requestParam.getRetryCount());

        Set<Instance> tried = new HashSet<>();
        IOException lastIoError = null;
        ResponseParam responseParam = new ResponseParam();
        Instance instance = firstInstance;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            if (attempt > 0) {
                // 重试：故障转移到另一个尚未失败的实例
                instance = selectInstance(candidates, tried);
                DispatchMetrics.recordRetry();
            }
            tried.add(instance);
            pointTo(requestParam, instance);

            Request okhttpRequest = requestBuild(requestParam);
            long startTime = System.currentTimeMillis();
            try (Response response = getClient().newCall(okhttpRequest).execute()) {
                long endTime = System.currentTimeMillis();
                responseParam = assembleResponseParam(response, endTime - startTime);
                DispatchMetrics.recordUpstreamResponse();
                CircuitBreakerRegistry.recordSuccess(instance.getInstanceId());
                DispatchInterceptorRegistry.postProcess(requestParam, instance, startTime, endTime, responseParam);
                // 拿到上游真实响应即返回，不再继续重试
                return responseParam;
            } catch (IOException ioException) {
                lastIoError = ioException;
                long endTime = System.currentTimeMillis();
                CircuitBreakerRegistry.recordFailure(instance.getInstanceId());
                LOGGER.warn("dispatch attempt {}/{} failed, instance: {}:{}, path: {}",
                        attempt + 1, maxAttempts, instance.getIp(), instance.getPort(),
                        requestParam.getUrlParam().getPath(), ioException);
                if (attempt + 1 >= maxAttempts) {
                    DispatchInterceptorRegistry.onProcessError(
                            requestParam, instance, startTime, endTime, ioException, responseParam);
                }
            }
        }

        // 所有尝试均未拿到上游响应：产出 502/504
        DispatchMetrics.recordForwardFailure();
        return gatewayErrorResponse(lastIoError);
    }

    /**
     * 将请求 URL 指向给定实例（故障转移时切换目标主机/端口）。
     */
    private static void pointTo(RequestParam requestParam, Instance instance) {
        UrlParam urlParam = requestParam.getUrlParam();
        urlParam.setHost(instance.getIp());
        urlParam.setPort(instance.getPort());
    }

    /**
     * 总转发尝试次数：1 次首发 + max(retryCount, 0) 次重试。retryCount 为负时按 0（不重试）处理。
     *
     * @param retryCount 重试次数
     * @return 总尝试次数（>= 1）
     */
    protected static int totalAttempts(int retryCount) {
        return 1 + Math.max(retryCount, 0);
    }

    /**
     * 配置的最大请求体字节数（0 表示不限制）。
     */
    protected static long maxBodyLimit() {
        return getDispatchProperty() == null ? 0L : getDispatchProperty().maxBodyBytes();
    }

    /**
     * 请求体是否超过限制（纯函数，便于测试）。maxBytes &lt;= 0 表示不限制。
     *
     * @param contentLength 请求声明的 Content-Length（未知为 -1）
     * @param maxBytes      最大字节数
     * @return 是否超限
     */
    static boolean exceedsBodyLimit(long contentLength, long maxBytes) {
        return maxBytes > 0 && contentLength > maxBytes;
    }

    /**
     * 构建网关错误响应：连接类异常 -> 502 Bad Gateway，超时类异常 -> 504 Gateway Timeout。
     *
     * @param error 最后一次 IO 异常，可能为 null
     * @return 带正确状态码与 JSON 错误体的响应参数
     */
    private static ResponseParam gatewayErrorResponse(IOException error) {
        boolean timeout = error instanceof InterruptedIOException;
        HttpStatus status = timeout ? HttpStatus.GATEWAY_TIMEOUT : HttpStatus.BAD_GATEWAY;

        String reason = Objects.isNull(error) || Objects.isNull(error.getMessage())
                ? status.getReasonPhrase() : error.getMessage();

        ResponseParam param = new ResponseParam();
        param.setSuccessful(false);
        param.setStatus(status);
        param.setBody(("{\"code\":" + status.value()
                + ",\"message\":\"trouve gateway forward failed: " + escapeJson(reason) + "\"}")
                .getBytes(StandardCharsets.UTF_8));
        return param;
    }

    /**
     * 以指定状态码与错误信息直接刷出错误响应（用于匹配/路由阶段的异常边界）。
     */
    protected static void flushError(ServletServerHttpResponse httpResponse,
                                     HttpStatus status, int code, String message) throws IOException {
        ResponseParam param = new ResponseParam();
        param.setSuccessful(false);
        param.setStatus(status);
        param.setBody(("{\"code\":" + code + ",\"message\":\"" + escapeJson(message) + "\"}")
                .getBytes(StandardCharsets.UTF_8));
        flush(httpResponse, param);
    }

    private static String escapeJson(String raw) {
        if (Objects.isNull(raw)) {
            return "";
        }
        return raw.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", " ").replace("\r", " ");
    }

    /**
     * 将结果刷入 response
     *
     * @param httpResponse
     * @param param
     * @throws IOException
     */
    protected static void flush(ServletServerHttpResponse httpResponse, ResponseParam param) throws IOException {
        httpResponse.setStatusCode(param.getStatus());

        writeHeaders(httpResponse, param);

        writeBody(httpResponse, param);

        httpResponse.flush();
    }
}
