package com.lei6393.trouve.server.common;

import org.springframework.stereotype.Indexed;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yulei
 * @date 2022/5/26 18:17
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Indexed
public @interface ConnectionPoolProperty {

    /**
     * 连接池最大连接数
     * {@link okhttp3.ConnectionPool}
     *
     * @return
     */
    int maxIdleConnections() default 10;

    /**
     * 连接保活时间，单位毫秒
     * {@link okhttp3.ConnectionPool}
     *
     * @return
     */
    long keepAliveDuration() default 120 * 1000;
}
