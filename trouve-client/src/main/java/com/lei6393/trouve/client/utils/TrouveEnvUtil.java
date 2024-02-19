package com.lei6393.trouve.client.utils;

import com.lei6393.trouve.client.common.Constants;
import com.lei6393.trouve.client.common.EnvProperties;
import com.lei6393.trouve.core.utils.EnvUtil;
import com.lei6393.trouve.core.utils.InetUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * @author leiyu
 * @date 2022/5/26 16:26
 */
public class TrouveEnvUtil {

    private static String selfIP;

    private static int port = -1;

    static {
        Runnable runnable = () -> {
            selfIP = InetUtil.getDefaultSelfIp(EnvProperties.TROUVE_CLIENT_IP_PROPERTY);
        };

        runnable.run();
    }

    public static String getSelfIP() {
        return selfIP;
    }


    public static int getPort() {
        if (port == -1) {
            String serverPort = EnvUtil.getEnv(EnvProperties.TROUVE_CLIENT_PORT_PROPERTY);
            port = StringUtils.isNotBlank(serverPort) ? Integer.parseInt(serverPort) : getDefaultPort();
        }
        return port;
    }

    private static int getDefaultPort() {
        String serverPort = EnvUtil.getEnv(EnvProperties.SERVER_PORT_PROPERTY);
        return StringUtils.isNotBlank(serverPort) ? Integer.parseInt(serverPort) : Constants.DEFAULT_CLIENT_PORT;
    }

}
