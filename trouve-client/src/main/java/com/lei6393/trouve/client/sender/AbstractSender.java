package com.lei6393.trouve.client.sender;

import com.lei6393.trouve.client.bean.ServerAddressParam;
import com.lei6393.trouve.core.connection.CenterURI;
import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
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

    private ServerAddressParam realAddress() {
        return addressParams.stream().findAny().get();
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

    private boolean execute(Supplier<Boolean> supplier) {
        boolean flag = false;
        try {
            for (int index = 0; index < retryCount && !flag; index++) {
                flag = supplier.get();
            }
            if (flag) {
                LOGGER.info("sender execute data success, uri: {}", uri);
            } else {
                LOGGER.warn("sender execute data failed, flag is false, uri: {}", uri);
            }
        } catch (Exception e) {
            try {
                flag = supplier.get();
            } catch (Exception exception) {
                LOGGER.error("sender execute data error, uri: {}", uri, e);
            }
        }
        return flag;
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
