package com.lei6393.trouve.server.common;

import org.springframework.stereotype.Indexed;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author leiyu
 * @date 2022/5/26 18:17
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Indexed
public @interface DispatchHttpProperty {

    /**
     * 连接池配置
     * {@link okhttp3.ConnectionPool}
     *
     * @return
     */
    ConnectionPoolProperty pool() default @ConnectionPoolProperty;

    /**
     * 读超时时间，单位毫秒
     * {@link okhttp3.OkHttpClient.Builder#readTimeout(long, TimeUnit)}
     *
     * @return
     */
    int readTimeout() default 5000;


    /**
     * 写超时时间，单位毫秒
     * {@link okhttp3.OkHttpClient.Builder#writeTimeout(long, TimeUnit)}
     *
     * @return
     */
    int writeTimeout() default 5000;


    /**
     * 连接超时时间，单位毫秒
     * {@link okhttp3.OkHttpClient.Builder#connectTimeout(long, TimeUnit)}
     *
     * @return
     */
    int connectTimeout() default 2000;

    /**
     * 转发失败（连接异常 / 超时）时的重试次数，默认 0（不重试）。
     * <p>
     * v2.0 起重试会<b>故障转移</b>到另一个尚未失败的健康实例，仅建议对幂等请求开启。
     *
     * @return 重试次数（负值按 0 处理）
     */
    int retryCount() default 0;

    /**
     * 是否开启按实例熔断器，默认 false（关闭，行为与旧版一致）。
     * 开启后连续失败达到阈值的实例会被临时摘除，冷却后半开试探恢复。
     *
     * @return 是否开启熔断
     */
    boolean circuitBreakerEnabled() default false;

    /**
     * 触发熔断的连续失败阈值，默认 5。
     *
     * @return 失败阈值
     */
    int circuitBreakerFailureThreshold() default 5;

    /**
     * 熔断打开后的冷却时长（毫秒），冷却期满进入半开试探，默认 10000。
     *
     * @return 冷却时长（毫秒）
     */
    long circuitBreakerOpenMillis() default 10000;

    /**
     * 网关入口最大并发在途转发数，默认 0（不限制，行为与旧版一致）。
     * 大于 0 时超过该并发的请求快速失败 503 削峰。
     *
     * @return 最大并发数
     */
    int maxConcurrentRequests() default 0;

    /**
     * 允许转发的最大请求体字节数，默认 0（不限制，行为与旧版一致）。
     * 大于 0 时，Content-Length 超过该值的请求快速失败 413，缓解整体缓冲导致的 OOM。
     *
     * @return 最大请求体字节数
     */
    long maxBodyBytes() default 0;
}
