package com.lei6393.trouve.server.dispatch;

import com.lei6393.trouve.core.data.instance.Instance;
import com.lei6393.trouve.server.bean.MatchPackage;
import com.lei6393.trouve.server.bean.request.PartParam;
import com.lei6393.trouve.server.bean.request.RequestParam;
import com.lei6393.trouve.server.bean.request.UrlParam;
import com.lei6393.trouve.server.bean.response.ResponseParam;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.support.MultipartResolutionDelegate;
import org.springframework.web.multipart.support.RequestPartServletServerHttpRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * @author yulei
 * @date 2022/5/25 22:13
 */
public class DispatchComposeHelper extends DispatchNetworkHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrouveRequestDispatcher.class);

    /**
     * 组装请求参数包
     *
     * @param request  request 请求
     * @param instance 实例
     * @return 请求参数包
     */
    public static RequestParam assembleRequestParam(@NotNull HttpServletRequest request,
                                                    @NotNull MatchPackage matchPackage,
                                                    Instance instance) {
        ServletServerHttpRequest httpRequest = new ServletServerHttpRequest(request);

        RequestParam requestParam = new RequestParam();

        requestParam.setUrlParam(assembleUrlParam(httpRequest, matchPackage, instance));

        requestParam.setMethod(httpRequest.getMethod());

        requestParam.setMediaType(httpRequest.getHeaders().getContentType());

        requestParam.setHeaders(httpRequest.getHeaders());

        assembleBody(request, requestParam, instance);

        DispatchInterceptorRegistry.preProcess(requestParam, httpRequest, instance);

        return requestParam;
    }

    /**
     * 组装返回参数包
     *
     * @param response 返回 response
     * @return
     * @throws IOException
     */
    public static ResponseParam assembleResponseParam(@NotNull Response response, long duration) throws IOException {
        ResponseParam responseParam = new ResponseParam();

        responseParam.setSuccessful(response.isSuccessful());

        responseParam.setStatus(HttpStatus.valueOf(response.code()));

        responseParam.setHeaders(assembleHeaders(response));

        responseParam.setBody(assembleBody(response));

        responseParam.setDuration(duration);

        return responseParam;
    }

    /**
     * 组装 URL
     *
     * @param httpRequest
     * @param instance
     * @return
     */
    private static UrlParam assembleUrlParam(@NotNull ServletServerHttpRequest httpRequest,
                                             @NotNull MatchPackage matchPackage,
                                             Instance instance) {
        URI uri = httpRequest.getURI();

        UrlParam param = new UrlParam();
        param.setSchema(uri.getScheme());
        param.setHost(instance.getIp());
        param.setPort(instance.getPort());
        param.setPath(StringUtils.defaultIfBlank(matchPackage.getLookupPath(), uri.getPath()));
        param.setQuery(uri.getQuery());
        return param;
    }


    private static MultiValueMap<String, String> assembleHeaders(Response response) {
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        Iterator<kotlin.Pair<String, String>> iterator = response.headers().iterator();
        while (iterator.hasNext()) {
            kotlin.Pair<String, String> header = iterator.next();
            param.add(header.getFirst(), header.getSecond());
        }
        return param;
    }

    private static void assembleBody(@NotNull HttpServletRequest request,
                                     @NotNull RequestParam requestParam,
                                     Instance instance) {
        boolean isMultiPart = MultipartResolutionDelegate.isMultipartRequest(request);
        requestParam.setMultiPart(isMultiPart);
        if (!isMultiPart) {
            requestParam.setBody(readBody(request));
        } else {
            requestParam.setParts(readMultiPart(request));
        }
    }

    private static byte[] readBody(@NotNull HttpServletRequest request) {
        ServletServerHttpRequest httpRequest = new ServletServerHttpRequest(request);
        byte[] body = null;
        try {
            body = IOUtils.toByteArray(httpRequest.getBody());
        } catch (Exception e) {
            LOGGER.error("read request body input stream error!", e);
        }
        return body;
    }

    private static Map<String, PartParam> readMultiPart(@NotNull HttpServletRequest request) {
        Map<String, PartParam> parts = new HashMap<>();
        try {
            for (Part part : request.getParts()) {
                String name = part.getName();

                PartParam partParam = new PartParam();
                HttpHeaders headers = null;
                byte[] body = null;
                try {
                    RequestPartServletServerHttpRequest partRequest =
                            new RequestPartServletServerHttpRequest(request, name);
                    headers = partRequest.getHeaders();
                    body = IOUtils.toByteArray(partRequest.getBody());
                } catch (Exception e) {
                    LOGGER.error("read request multi part body input stream error!", e);
                }

                partParam.setHeaders(headers);
                partParam.setBody(body);
                parts.put(name, partParam);
            }
        } catch (Exception e) {
            LOGGER.error("read request multi part error!", e);
        }
        return parts;
    }

    private static byte[] assembleBody(Response response) throws IOException {
        byte[] body = null;
        ResponseBody responseBody = response.body();
        if (Objects.nonNull(responseBody)) {
            body = responseBody.bytes();
        }
        return body;
    }

}
