package com.lei6393.trouve.server.dispatch;

import com.lei6393.trouve.core.utils.EnvUtil;
import com.lei6393.trouve.server.common.EnvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

/**
 * 网关优雅停机生命周期。
 * <p>
 * 在 Spring 容器关闭时（先于转发基础设施销毁）进入 {@link GatewayDrain} draining，
 * 等待在途转发完成或超时（{@code trouve.server.shutdown-drain-millis}，默认 30000ms）。
 *
 * @author trouve
 */
public class TrouveGatewayLifecycle implements SmartLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrouveGatewayLifecycle.class);

    private static final long DEFAULT_DRAIN_MILLIS = 30_000L;

    private volatile boolean running = false;

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop() {
        running = false;
        GatewayDrain.startDraining();
        long drainMillis = EnvUtil.getProperty(
                EnvProperties.TROUVE_SERVER_SHUTDOWN_DRAIN_MILLIS, Long.class, DEFAULT_DRAIN_MILLIS);
        boolean drained = GatewayDrain.awaitDrain(drainMillis);
        LOGGER.info("trouve gateway shutdown drain: drained={}, remaining in-flight={}",
                drained, GatewayDrain.inFlight());
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        // 高 phase 在关闭时最先停止，确保 drain 先于其它资源销毁
        return Integer.MAX_VALUE;
    }
}
