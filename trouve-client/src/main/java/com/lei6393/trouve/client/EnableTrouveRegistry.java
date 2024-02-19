package com.lei6393.trouve.client;

import com.lei6393.trouve.client.common.EnvProperties;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Indexed;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开启服务发现
 * 可配置属性详见 {@link EnvProperties }
 *
 * @author yulei
 * @date 2022/5/16 10:01
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Indexed
@Import({TrouveRegistryConfiguration.class, DefaultTrouveRegistryConfiguration.class})
public @interface EnableTrouveRegistry {

    /**
     * service name
     *
     * @return
     */
    String value();

    /**
     * 服务端地址，优先获取配置属性{@link EnvProperties#TROUVE_SERVER_ADDRESS},如果没有该属性，则采用该注解值
     *
     * @return
     */
    ServerAddress[] serverAddresses() default {};

    /**
     * 心跳间隔
     *
     * @return 间隔（毫秒）
     */
    long heartRateInterval() default 5 * 1000;

    /**
     * meta数据更新间隔
     *
     * @return 间隔（毫秒）
     */
    long metaUpdateInterval() default 10 * 60 * 1000;
}
