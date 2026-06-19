package com.lei6393.trouve.server.controller;

import com.lei6393.trouve.core.utils.GsonUtil;
import com.lei6393.trouve.server.TrouveLoader;
import com.lei6393.trouve.server.auth.RegistryAuthenticator;
import com.lei6393.trouve.server.consistency.AbstractHealthChecker;
import com.lei6393.trouve.server.dispatch.metrics.DispatchMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;

@RestController
@RequestMapping("/trouve/manager")
public class ManagerController {

    @Autowired
    AbstractHealthChecker healthChecker;

    @RequestMapping(value = "/health/instances", produces = "application/json")
    public ResponseEntity<String> getHealthInstances(HttpServletRequest request) {
        if (!RegistryAuthenticator.authenticate(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(GsonUtil.INSTANCE.toJson(healthChecker.getHealthInstanceIds()));
    }

    /**
     * 转发链路内置指标快照（请求 / 上游响应 / 转发失败 / 重试 / 限流拒绝计数）。
     */
    @RequestMapping(value = "/metrics", produces = "application/json")
    public ResponseEntity<String> getMetrics(HttpServletRequest request) {
        if (!RegistryAuthenticator.authenticate(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(GsonUtil.INSTANCE.toJson(DispatchMetrics.snapshot()));
    }

    /**
     * Prometheus 文本曝光格式的转发指标。
     */
    @RequestMapping(value = "/prometheus", produces = "text/plain;version=0.0.4;charset=utf-8")
    public ResponseEntity<String> getPrometheus(HttpServletRequest request) {
        if (!RegistryAuthenticator.authenticate(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(DispatchMetrics.prometheus());
    }

    /**
     * 轻量管理面板（自包含 HTML，每 5 秒自动刷新）。
     */
    @RequestMapping(value = "/dashboard", produces = "text/html;charset=utf-8")
    public ResponseEntity<String> dashboard(HttpServletRequest request) {
        if (!RegistryAuthenticator.authenticate(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.TEXT_PLAIN).body("unauthorized");
        }
        Collection<String> health = healthChecker.getHealthInstanceIds();
        String html = DashboardRenderer.render(
                TrouveLoader.getNamespace(),
                DispatchMetrics.snapshot(),
                health == null ? Collections.emptyList() : health);
        return ResponseEntity.ok(html);
    }

}
