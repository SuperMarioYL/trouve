package com.lei6393.trouve.client;

import com.lei6393.trouve.client.bean.ServerAddressParam;
import com.lei6393.trouve.client.common.EnvProperties;
import com.lei6393.trouve.client.data.instance.InstanceFactory;
import com.lei6393.trouve.client.data.meta.MetaFactory;
import com.lei6393.trouve.client.sender.HeartBeatSender;
import com.lei6393.trouve.client.sender.MetaSender;
import com.lei6393.trouve.core.exception.TrouveException;
import com.lei6393.trouve.core.utils.EnvUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author yulei
 * @date 2022/5/25 00:58
 */
public abstract class ScheduledRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledRegistry.class);

    private final Timer timer = new Timer();

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

            // 心跳和元信息注册
            heartBeatSender.register(instanceFactory.create());
            metaSender.register(metaFactory.create());
        } catch (Exception e) {
            LOGGER.error("schedule register error!", e);
        }

        // 定时发送心跳信息
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    heartBeatSender.update(instanceFactory.create());
                } catch (Exception e) {
                    LOGGER.error("heart beat error!", e);
                }
            }
        }, 0, heartRateInterval);

        // 定时 meta 信息
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    metaSender.update(metaFactory.create());
                } catch (Exception e) {
                    LOGGER.error("meta update error!", e);
                }
            }
        }, metaUpdateInterval, metaUpdateInterval);

        // jvm 关闭时删除 instance
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
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
