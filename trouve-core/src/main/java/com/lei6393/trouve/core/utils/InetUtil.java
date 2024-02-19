package com.lei6393.trouve.core.utils;

import com.google.common.net.InetAddresses;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author yulei
 * @date 2022/5/22 23:57
 */
public class InetUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(InetUtil.class);

    public static String getDefaultSelfIp(String priorityProperty) {
        String trouveIp = EnvUtil.getEnv(priorityProperty);
        InetAddress inetAddress;
        if (StringUtils.isNotBlank(trouveIp) && !InetAddresses.isInetAddress(trouveIp)) {
            try {
                inetAddress = InetAddress.getByName(trouveIp);
                trouveIp = inetAddress.getCanonicalHostName();
            } catch (UnknownHostException unknownHostException) {
                throw new RuntimeException("trouve address " + trouveIp + " is not ip");
            }
        }
        if (StringUtils.isBlank(trouveIp)) {
            try {
                inetAddress = InetAddress.getLocalHost();
                trouveIp = inetAddress.getCanonicalHostName();
            } catch (UnknownHostException ignore) {
                LOGGER.warn("Unable to retrieve localhost");
            }
        }
        return trouveIp;
    }
}
