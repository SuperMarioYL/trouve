package com.lei6393.trouve.server.auth;

import com.lei6393.trouve.core.Constants;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 控制面（注册 / 心跳 / 元信息）共享令牌鉴权。
 * <p>
 * 默认<b>关闭</b>：未配置 {@code trouve.server.token} 时放行所有请求（与旧版兼容）。
 * 配置令牌后，client 必须在 {@link Constants#TROUVE_TOKEN_HEADER} 头携带匹配令牌，
 * 否则注册 / 心跳 / 元信息接口返回 401，防止任意主机注册恶意实例劫持路由。
 *
 * @author trouve
 */
public final class RegistryAuthenticator {

    private static volatile String token = null;

    private RegistryAuthenticator() {
    }

    public static void configure(String token) {
        RegistryAuthenticator.token = StringUtils.trimToNull(token);
    }

    public static boolean isEnabled() {
        return token != null;
    }

    /**
     * 校验请求是否通过控制面鉴权。未配置令牌时恒放行。
     *
     * @param request 控制面请求
     * @return 是否放行
     */
    public static boolean authenticate(HttpServletRequest request) {
        String presented = request == null ? null : request.getHeader(Constants.TROUVE_TOKEN_HEADER);
        return authenticateToken(presented);
    }

    /**
     * 校验给定令牌（servlet 无关核心，便于测试）。未配置令牌时恒放行。
     *
     * @param presented client 提供的令牌
     * @return 是否放行
     */
    public static boolean authenticateToken(String presented) {
        String expected = token;
        if (expected == null) {
            return true;
        }
        return constantTimeEquals(expected, presented);
    }

    /**
     * 常量时间比较，避免令牌比较的时序侧信道。
     */
    private static boolean constantTimeEquals(String expected, String presented) {
        if (presented == null || expected.length() != presented.length()) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < expected.length(); i++) {
            diff |= expected.charAt(i) ^ presented.charAt(i);
        }
        return diff == 0;
    }
}
