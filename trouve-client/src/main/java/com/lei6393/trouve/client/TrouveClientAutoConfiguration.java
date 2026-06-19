package com.lei6393.trouve.client;

import com.lei6393.trouve.client.common.EnvProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Trouve client 端 spring-boot-starter 自动装配。
 * <p>
 * 设置 {@code trouve.client.service-name} 即可启用注册，无需 {@code @EnableTrouveRegistry} 注解；
 * 服务名 / 间隔 / 服务端地址由 {@link TrouveRegistry} 的 properties-only 路径加载。
 *
 * @author trouve
 */
@Configuration
@ConditionalOnProperty(name = EnvProperties.TROUVE_CLIENT_SERVICE_NAME)
@Import({TrouveRegistryConfiguration.class, DefaultTrouveRegistryConfiguration.class})
public class TrouveClientAutoConfiguration {
}
