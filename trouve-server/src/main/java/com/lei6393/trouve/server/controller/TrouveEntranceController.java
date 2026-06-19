package com.lei6393.trouve.server.controller;

import com.lei6393.trouve.server.dispatch.TrouveRequestDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 内置的 catch-all 转发入口控制器（starter 可选自动注册）。
 * <p>
 * 通过 {@code trouve.server.auto-entrance=true} 开启后无需手写 EntranceController；
 * 默认关闭，避免与用户自定义的 {@code @RequestMapping("**")} 控制器冲突（双重映射）。
 *
 * @author trouve
 */
@RestController
public class TrouveEntranceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrouveEntranceController.class);

    @RequestMapping("/**")
    public void entrance(HttpServletRequest request, HttpServletResponse response) {
        try {
            TrouveRequestDispatcher.entrance(request, response);
        } catch (Throwable e) {
            LOGGER.error("trouve entrance error!", e);
        }
    }
}
