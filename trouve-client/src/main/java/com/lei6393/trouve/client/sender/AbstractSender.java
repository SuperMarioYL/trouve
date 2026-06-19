package com.lei6393.trouve.client.sender;

import com.lei6393.trouve.client.bean.ServerAddressParam;
import com.lei6393.trouve.core.Constants;
import com.lei6393.trouve.core.connection.CenterURI;
import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * @author leiyu
 * @date 2022/5/24 23:07
 */
public abstract class AbstractSender<DATA> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSender.class);

    private final List<ServerAddressParam> addressParams;

    private String uri;

    private final OkHttpClient client;

    private int retryCount = 2;

    /**
     * 注册中心地址轮询游标，使重试时切换到下一个地址，实现多注册中心故障转移。
     */
    private final AtomicInteger addressCursor = new AtomicInteger(0);

    /**
     * 控制面鉴权令牌，配置后随注册 / 心跳 / 元信息请求一起发送。
     */
    private volatile String token;

    public AbstractSender(List<ServerAddressParam> addressParams, CenterURI centerURI) {
        this.addressParams = addressParams;
        this.uri = centerURI.getUri();
        this.client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(addressParams.size(), 5, TimeUnit.MINUTES))
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();
    }

    public HttpUrl realUrl() {
        ServerAddressParam addressParam = realAddress();
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme(addressParam.getSchema())
                .host(addressParam.getHost())
                .port(addressParam.getPort())
                .encodedPath(getUri())
                .build();

        return httpUrl;
    }

    /**
     * 构建已带控制面鉴权头（如配置）和目标地址的请求 builder。
     */
    protected Request.Builder authedBuilder() {
        Request.Builder builder = new Request.Builder().url(realUrl());
        String currentToken = token;
        if (StringUtils.isNotBlank(currentToken)) {
            builder.header(Constants.TROUVE_TOKEN_HEADER, currentToken);
        }
        return builder;
    }

    public void setToken(String token) {
        this.token = StringUtils.trimToNull(token);
    }

    /**
     * 轮询选取一个注册中心地址。配合 {@link #execute(Supplier)} 的重试，
     * 单个注册中心宕机时下一次尝试会自动切换到其它地址，实现故障转移。
     */
    private ServerAddressParam realAddress() {
        if (addressParams.isEmpty()) {
            throw new IllegalStateException("trouve server addresses is empty");
        }
        int index = Math.floorMod(addressCursor.getAndIncrement(), addressParams.size());
        return addressParams.get(index);
    }

    public boolean register(DATA data) {
        return execute(() -> realRegister(data));
    }

    public boolean update(DATA data) {
        return execute(() -> realUpdate(data));
    }

    public boolean remove(DATA data) {
        return execute(() -> realRemove(data));
    }

    /**
     * 确定性重试：最多尝试 retryCount 次，任一次成功立即返回；
     * 每次尝试经 {@link #realAddress()} 轮询不同注册中心地址，实现故障转移。
     * 失败时记录最后一次真实异常，避免静默吞掉错误导致注册漂移。
     */
    private boolean execute(Supplier<Boolean> supplier) {
        Exception lastError = null;
        for (int attempt = 0; attempt < retryCount; attempt++) {
            try {
                if (supplier.get()) {
                    LOGGER.info("sender execute data success, uri: {}", uri);
                    return true;
                }
            } catch (Exception e) {
                lastError = e;
            }
        }
        if (lastError != null) {
            LOGGER.error("sender execute data error after {} attempts, uri: {}", retryCount, uri, lastError);
        } else {
            LOGGER.warn("sender execute data failed after {} attempts, uri: {}", retryCount, uri);
        }
        return false;
    }

    protected abstract boolean realRegister(DATA data);

    protected abstract boolean realUpdate(DATA data);

    protected abstract boolean realRemove(DATA data);

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public OkHttpClient getClient() {
        return client;
    }
}
