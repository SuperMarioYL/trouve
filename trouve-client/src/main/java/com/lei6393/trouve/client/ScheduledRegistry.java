package com.lei6393.trouve.client;

import com.lei6393.trouve.client.bean.ServerAddressParam;
import com.lei6393.trouve.client.common.EnvProperties;
import com.lei6393.trouve.client.data.instance.InstanceFactory;
import com.lei6393.trouve.client.data.meta.MetaFactory;
import com.lei6393.trouve.client.sender.HeartBeatSender;
import com.lei6393.trouve.client.sender.MetaSender;
import com.lei6393.trouve.core.exception.TrouveException;
import com.lei6393.trouve.core.utils.EnvUtil;
import com.lei6393.trouve.core.utils.TrouveScheduler;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author leiyu
 * @date 2022/5/25 00:58
 */
public abstract class ScheduledRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledRegistry.class);

    private final ScheduledExecutorService scheduler = TrouveScheduler.newSingleThread("trouve-registry-scheduler");

    private List<ServerAddressParam> addressParams = new ArrayList<>();

    private long heartRateInterval;

    private long metaUpdateInterval;

    private HeartBeatSender heartBeatSender;

    private MetaSender metaSender;

    private InstanceFactory instanceFactory;

    private MetaFactory metaFactory;

    public void scheduleStart() {
        try {
            if (CollectionUtils.isEmpty(addressParams)) {
                throw new TrouveException("server addresses is empty.");
            }
            // 初始化心跳发送器和元信息发送器
            heartBeatSender = new HeartBeatSender(addressParams);
            metaSender = new MetaSender(addressParams);

            // 控制面鉴权令牌（如配置）
            String token = EnvUtil.getEnv(EnvProperties.TROUVE_CLIENT_TOKEN);
            heartBeatSender.setToken(token);
            metaSender.setToken(token);

            // 心跳和元信息注册
            heartBeatSender.register(instanceFactory.create());
            metaSender.register(metaFactory.create());
        } catch (Exception e) {
            LOGGER.error("schedule register error!", e);
        }

        // 定时发送心跳信息
        scheduler.scheduleWithFixedDelay(TrouveScheduler.guard("heart-beat", () ->
                heartBeatSender.update(instanceFactory.create())), 0, heartRateInterval, TimeUnit.MILLISECONDS);

        // 定时 meta 信息
        scheduler.scheduleWithFixedDelay(TrouveScheduler.guard("meta-update", () ->
                metaSender.update(metaFactory.create())), metaUpdateInterval, metaUpdateInterval, TimeUnit.MILLISECONDS);

        // jvm 关闭时停止调度并注销 instance
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.shutdownNow();
            heartBeatSender.remove(instanceFactory.create());
        }, "trouve-shutdown-instance-thread"));
    }

    public void loadServerAddresses(EnableTrouveRegistry enableTrouveRegistry) {
        List<String> uris = EnvUtil.getEnvList(EnvProperties.TROUVE_SERVER_ADDRESS);
        if (CollectionUtils.isNotEmpty(uris)) {
            for (String uriStr : uris) {
                addressParams.add(ServerAddressParam.of(uriStr));
            }
        } else {
            for (ServerAddress address : enableTrouveRegistry.serverAddresses()) {
                addressParams.add(ServerAddressParam.of(address));
            }
        }
    }

    /**
     * 仅从配置属性 {@code trouve.server.address} 加载服务端地址（starter 无注解路径）。
     */
    public void loadServerAddressesFromEnv() {
        List<String> uris = EnvUtil.getEnvList(EnvProperties.TROUVE_SERVER_ADDRESS);
        if (CollectionUtils.isNotEmpty(uris)) {
            for (String uriStr : uris) {
                addressParams.add(ServerAddressParam.of(uriStr));
            }
        }
    }

    public void setAddressParams(List<ServerAddressParam> addressParams) {
        this.addressParams = addressParams;
    }

    public void setHeartRateInterval(long heartRateInterval) {
        this.heartRateInterval = heartRateInterval;
    }

    public void setMetaUpdateInterval(long metaUpdateInterval) {
        this.metaUpdateInterval = metaUpdateInterval;
    }

    public void setFactory(InstanceFactory instanceFactory,
                           MetaFactory metaFactory) {
        this.instanceFactory = instanceFactory;
        this.metaFactory = metaFactory;
    }
}
