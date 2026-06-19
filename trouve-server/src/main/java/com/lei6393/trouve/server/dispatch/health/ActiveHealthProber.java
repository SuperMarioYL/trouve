package com.lei6393.trouve.server.dispatch.health;

import com.lei6393.trouve.core.data.instance.Instance;
import com.lei6393.trouve.core.utils.TrouveScheduler;
import com.lei6393.trouve.server.consistency.Matcher;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 主动 HTTP 健康探活器（opt-in）。
 * <p>
 * 周期性对路由表中的每个实例发起轻量 HTTP GET 探测，按 2xx/非2xx/IO 异常把结果记入
 * {@link ActiveHealthRegistry}（带滞回）。独立有界超时，避免拖垮转发线程。
 *
 * @author trouve
 */
public class ActiveHealthProber {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActiveHealthProber.class);

    private final ScheduledExecutorService scheduler;

    private final OkHttpClient client;

    private final String path;

    public ActiveHealthProber(String path, long intervalMillis) {
        this.path = path;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS)
                .build();
        this.scheduler = TrouveScheduler.newSingleThread("trouve-active-health-prober");
        this.scheduler.scheduleWithFixedDelay(
                TrouveScheduler.guard("active-health-probe", this::probeAll),
                intervalMillis, intervalMillis, TimeUnit.MILLISECONDS);
    }

    private void probeAll() {
        for (Instance instance : Matcher.allInstances()) {
            probe(instance);
        }
    }

    private void probe(Instance instance) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host(instance.getIp())
                .port(instance.getPort())
                .encodedPath(path)
                .build();
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                ActiveHealthRegistry.recordSuccess(instance.getInstanceId());
            } else {
                ActiveHealthRegistry.recordFailure(instance.getInstanceId());
            }
        } catch (Exception e) {
            ActiveHealthRegistry.recordFailure(instance.getInstanceId());
            LOGGER.debug("active health probe failed for {}:{}", instance.getIp(), instance.getPort());
        }
    }
}
