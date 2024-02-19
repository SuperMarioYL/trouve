package com.lei6393.trouve.server.common;

import org.springframework.stereotype.Indexed;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author yulei
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
}
