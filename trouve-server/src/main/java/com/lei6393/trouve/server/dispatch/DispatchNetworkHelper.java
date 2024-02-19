package com.lei6393.trouve.server.dispatch;

import com.lei6393.trouve.core.exception.TrouveException;
import com.lei6393.trouve.core.exception.TrouveNullRequestBodyException;
import com.lei6393.trouve.server.bean.request.PartParam;
import com.lei6393.trouve.server.bean.request.RequestParam;
import com.lei6393.trouve.server.bean.request.UrlParam;
import com.lei6393.trouve.server.bean.response.ResponseParam;
import com.lei6393.trouve.server.common.ConnectionPoolProperty;
import com.lei6393.trouve.server.common.DispatchHttpProperty;
import okhttp3.ConnectionPool;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 分发器 HTTP 配置
 *
 * @author leiyu
 * @date 2022/5/25 22:16
 */
public class DispatchNetworkHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DispatchNetworkHelper.class);

    private static OkHttpClient client;

    private static DispatchHttpProperty property;

    /**
     * 获取 HTTP 客户端
     *
     * @return HTTP 客户端
     */
    public static OkHttpClient getClient() {
        if (Objects.isNull(client)) {
            ConnectionPoolProperty poolProperty = property.pool();
            ConnectionPool pool = new ConnectionPool(
                    poolProperty.maxIdleConnections(),
                    poolProperty.keepAliveDuration(),
                    TimeUnit.MILLISECONDS
            );

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectionPool(pool)
                    .readTimeout(property.readTimeout(), TimeUnit.MILLISECONDS)
                    .writeTimeout(property.writeTimeout(), TimeUnit.MILLISECONDS)
                    .connectTimeout(property.connectTimeout(), TimeUnit.MILLISECONDS)
                    .build();
            client = okHttpClient;
        }
        return client;
    }

    /**
     * 请求构建
     *
     * @param requestParam
     * @return
     */
    protected static Request requestBuild(RequestParam requestParam) throws TrouveException {
        Request.Builder requestBuilder = new Request.Builder()
                .url(urlBuild(requestParam))
                .headers(headersBuild(requestParam));

        bodyFill(requestBuilder, requestParam);

        return requestBuilder.build();
    }

    /**
     * 写入返回头
     *
     * @param httpResponse
     * @param responseParam
     */
    protected static void writeHeaders(@NotNull ServletServerHttpResponse httpResponse,
                                       ResponseParam responseParam) {
        MultiValueMap<String, String> map = responseParam.getHeaders();
        if (MapUtils.isEmpty(map)) {
            return;
        }
        HttpHeaders headers = httpResponse.getHeaders();
        headers.addAll(map);
    }

    /**
     * 写入返回体
     *
     * @param httpResponse
     * @param responseParam
     * @throws IOException
     */
    protected static void writeBody(@NotNull ServletServerHttpResponse httpResponse,
                                    ResponseParam responseParam) throws IOException {
        if (Objects.nonNull(responseParam.getBody())) {
            IOUtils.write(responseParam.getBody(), httpResponse.getBody());
        }
    }


    /**
     * url 构建
     *
     * @param requestParam
     * @return
     */
    private static HttpUrl urlBuild(RequestParam requestParam) {
        UrlParam param = requestParam.getUrlParam();
        HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                .scheme(param.getSchema())
                .host(param.getHost())
                .port(param.getPort())
                .encodedPath(param.getPath())
                .encodedQuery(param.getQuery());

        return urlBuilder.build();
    }

    /**
     * 请求头组装
     *
     * @param requestParam
     * @return
     */
    private static Headers headersBuild(RequestParam requestParam) {
        return convertHttpHeaders(requestParam.getHeaders());
    }

    /**
     * request body 填充
     *
     * @param requestBuilder
     * @param requestParam
     */
    private static void bodyFill(Request.Builder requestBuilder, RequestParam requestParam) throws TrouveException {
        HttpMethod method = requestParam.getMethod();
        RequestBody body;
        switch (method) {
            case GET:
                requestBuilder.get();
                break;

            case HEAD:
                requestBuilder.head();
                break;

            case POST:
                body = extractBody(requestParam);
                if (Objects.nonNull(body)) {
                    requestBuilder.post(body);
                } else {
                    throw new TrouveNullRequestBodyException();
                }
                break;

            case DELETE:
                body = extractBody(requestParam);
                requestBuilder.delete(body);
                break;

            case PUT:
                body = extractBody(requestParam);
                if (Objects.nonNull(body)) {
                    requestBuilder.put(body);
                } else {
                    throw new TrouveNullRequestBodyException();
                }
                break;

            case PATCH:
                body = extractBody(requestParam);
                if (Objects.nonNull(body)) {
                    requestBuilder.patch(body);
                } else {
                    throw new TrouveNullRequestBodyException();
                }
                break;

            default:
                break;
        }
    }

    private static RequestBody extractBody(RequestParam requestParam) {
        if (Objects.isNull(requestParam.getMediaType())) {
            return null;
        }
        MediaType mediaType = MediaType.parse(requestParam.getMediaType().toString());
        RequestBody body;
        if (Objects.isNull(mediaType)) {
            return null;
        }
        if (!requestParam.isMultiPart()) {
            body = RequestBody.create(requestParam.getBody(), mediaType);
        } else {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(mediaType);
            for (PartParam partParam : requestParam.getParts().values()) {
                org.springframework.http.MediaType type = partParam.getHeaders().getContentType();
                type = Objects.isNull(type) ? org.springframework.http.MediaType.ALL : type;
                MediaType partMediaType = MediaType.parse(type.toString());

                partParam.getHeaders().remove(HttpHeaders.CONTENT_TYPE);
                partParam.getHeaders().remove(HttpHeaders.CONTENT_LENGTH);
                Headers partHeaders = convertHttpHeaders(partParam.getHeaders());

                RequestBody partBody = RequestBody.create(partParam.getBody(), partMediaType);

                MultipartBody.Part part = MultipartBody.Part.create(partHeaders, partBody);
                builder.addPart(part);
            }
            body = builder.build();
        }
        return body;
    }

    private static Headers convertHttpHeaders(HttpHeaders httpHeaders) {
        Headers.Builder headersBuilder = new Headers.Builder();
        if (!httpHeaders.isEmpty()) {
            for (Map.Entry<String, List<String>> header : httpHeaders.entrySet()) {
                for (String value : header.getValue()) {
                    headersBuilder.addUnsafeNonAscii(header.getKey(), value);
                }
            }
        }
        return headersBuilder.build();
    }

    public static void loadProperty(DispatchHttpProperty httpProperty) {
        property = httpProperty;
    }
}
