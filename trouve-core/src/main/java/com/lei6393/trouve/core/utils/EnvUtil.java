package com.lei6393.trouve.core.utils;

import com.sun.management.OperatingSystemMXBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.ConfigurableEnvironment;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author leiyu
 * @date 2022/5/22 23:57
 */
public class EnvUtil {

    private static final OperatingSystemMXBean OPERATING_SYSTEM_MX_BEAN =
            (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    private static ConfigurableEnvironment environment;


    public static ConfigurableEnvironment getEnvironment() {
        return environment;
    }

    public static void setEnvironment(ConfigurableEnvironment environment) {
        EnvUtil.environment = environment;
    }

    public static boolean containsProperty(String key) {
        return environment.containsProperty(key);
    }

    public static String getEnv(String key) {
        String value = getProperty(key);
        if (StringUtils.isBlank(value)) {
            value = getSystemEnv(key);
        }
        return value;
    }

    public static String getProperty(String key) {
        return environment.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return environment.getProperty(key, defaultValue);
    }

    public static <T> T getProperty(String key, Class<T> targetType) {
        return environment.getProperty(key, targetType);
    }

    public static <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        return environment.getProperty(key, targetType, defaultValue);
    }

    public static String getRequiredProperty(String key) throws IllegalStateException {
        return environment.getRequiredProperty(key);
    }

    public static <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
        return environment.getRequiredProperty(key, targetType);
    }

    public static String resolvePlaceholders(String text) {
        return environment.resolvePlaceholders(text);
    }

    public static String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
        return environment.resolveRequiredPlaceholders(text);
    }

    public static List<String> getEnvList(String key) {
        String temp = getEnv(key);
        if (StringUtils.isBlank(temp)) {
            return null;
        }
        List<String> valueList = Arrays.asList(StringUtils.split(temp, ","));
        return valueList;
    }

    public static List<String> getPropertyList(String key) {
        List<String> valueList = new ArrayList<>();

        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            String value = environment.getProperty(key + "[" + i + "]");
            if (StringUtils.isBlank(value)) {
                break;
            }

            valueList.add(value);
        }

        return valueList;
    }

    public static String getSystemEnv(String key) {
        return System.getProperty(key, System.getenv(key));
    }

    public static float getLoad() {
        return (float) OPERATING_SYSTEM_MX_BEAN.getSystemLoadAverage();
    }

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public static float getCPU() {
        return (float) OPERATING_SYSTEM_MX_BEAN.getSystemCpuLoad();
    }

    public static float getMem() {
        return (float) (1
                - (double) OPERATING_SYSTEM_MX_BEAN.getFreePhysicalMemorySize() / (double) OPERATING_SYSTEM_MX_BEAN
                .getTotalPhysicalMemorySize());
    }

}
