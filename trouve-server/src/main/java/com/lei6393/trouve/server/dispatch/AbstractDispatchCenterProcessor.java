package com.lei6393.trouve.server.dispatch;

import com.lei6393.trouve.core.data.instance.Instance;
import com.lei6393.trouve.core.exception.TrouveEmptyInstanceException;
import com.lei6393.trouve.core.exception.TrouveException;
import com.lei6393.trouve.server.bean.MatchPackage;
import com.lei6393.trouve.server.bean.request.RequestParam;
import com.lei6393.trouve.server.bean.response.ResponseParam;
import com.lei6393.trouve.server.consistency.Matcher;
import com.lei6393.trouve.server.loadbalance.Balancer;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServletServerHttpResponse;

import java.io.IOException;
import java.util.List;

/**
 * @author leiyu
 * @date 2022/5/25 23:45
 */
public abstract class AbstractDispatchCenterProcessor extends DispatchComposeHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDispatchCenterProcessor.class);

    /**
     * 获取最优匹配实例
     *
     * @param matchPackage
     * @return
     * @throws TrouveException
     */
    protected static Instance matchInstance(MatchPackage matchPackage) throws TrouveException {
        DispatchInterceptorRegistry.preMatch(matchPackage);

        // 获取匹配的实例
        List<Instance> instances = Matcher.getMatchInstance(matchPackage);
        if (CollectionUtils.isEmpty(instances)) {
            throw new TrouveEmptyInstanceException();
        }

        // 根据算法获取最优实例
        Instance instance = Balancer.balance(instances);

        return instance;
    }

    /**
     * 执行分发调用
     *
     * @param requestParam
     * @return
     * @throws IOException
     */
    protected static ResponseParam execute(@NotNull RequestParam requestParam,
                                           @NotNull Instance instance) throws Throwable {
        // 构建请求包
        Request okhttpRequest = requestBuild(requestParam);

        // 获取调用次数
        int callTimes = 1 + Math.min(requestParam.getRetryCount(), 0);

        // 真正调用
        ResponseParam responseParam = new ResponseParam();
        long startTime;
        long endTime;
        for (int hasCallTimes = 0; hasCallTimes < callTimes; hasCallTimes++) {

            startTime = System.currentTimeMillis();
            try (Response response = getClient().newCall(okhttpRequest).execute()) {
                endTime = System.currentTimeMillis();
                responseParam = assembleResponseParam(response, System.currentTimeMillis() - startTime);
                DispatchInterceptorRegistry.postProcess(requestParam, instance, startTime, endTime, responseParam);
            } catch (IOException ioException) {
                if (hasCallTimes + 1 >= callTimes) {
                    endTime = System.currentTimeMillis();
                    DispatchInterceptorRegistry.onProcessError(
                            requestParam, instance, startTime, endTime, ioException, responseParam);
                }
            } catch (Exception e) {
                endTime = System.currentTimeMillis();
                DispatchInterceptorRegistry.onProcessError(
                        requestParam, instance, startTime, endTime, e, responseParam);
            }
        }

        return responseParam;
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
