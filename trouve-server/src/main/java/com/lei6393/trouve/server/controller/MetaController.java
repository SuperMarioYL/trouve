package com.lei6393.trouve.server.controller;

import com.lei6393.trouve.core.data.MetaMsg;
import com.lei6393.trouve.core.data.instance.Instance;
import com.lei6393.trouve.core.utils.GsonUtil;
import com.lei6393.trouve.server.instence.InstancePreCheckerRegistry;
import com.lei6393.trouve.server.instence.generator.DefaultInstanceIdGenerator;
import com.lei6393.trouve.server.meta.MetaOperatorRegistry;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

/**
 * 实例 controller
 *
 * @author leiyu
 * @date 2022/5/18 23:46
 */
@RestController
@RequestMapping("/trouve/meta")
@SuppressWarnings("unchecked")
public class MetaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaController.class);

    @PostMapping()
    public ResponseEntity<String> register(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            MetaMsg metaMsg = buildMetaFromRequest(request);
            if (InstancePreCheckerRegistry.preChecker(metaMsg.getInstance())) {
                MetaOperatorRegistry.registerMeta(metaMsg);
                LOGGER.debug("register meta success, instance: {}", metaMsg.getInstance().getInstanceId());
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception e) {
            LOGGER.error("register meta error!", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping()
    public ResponseEntity<String> remove(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            MetaMsg metaMsg = buildMetaFromRequest(request);
            if (InstancePreCheckerRegistry.preChecker(metaMsg.getInstance())) {
                MetaOperatorRegistry.removeMeta(metaMsg);
                LOGGER.debug("remove meta success, instance: {}", metaMsg.getInstance().getInstanceId());
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception e) {
            LOGGER.error("remove meta error!", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping()
    public ResponseEntity<String> update(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            MetaMsg metaMsg = buildMetaFromRequest(request);
            if (InstancePreCheckerRegistry.preChecker(metaMsg.getInstance())) {
                MetaOperatorRegistry.updateMeta(metaMsg);
                LOGGER.debug("update meta success, instance: {}", metaMsg.getInstance().getInstanceId());
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception e) {
            LOGGER.error("update meta error!", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok().build();
    }

    private MetaMsg buildMetaFromRequest(HttpServletRequest request) {
        MetaMsg metaMsg = null;
        try {
            ServletServerHttpRequest httpRequest = new ServletServerHttpRequest(request);
            String body = IOUtils.toString(httpRequest.getBody(), StandardCharsets.UTF_8);
            metaMsg = GsonUtil.INSTANCE.fromJson(body, MetaMsg.class);
            Instance instance = metaMsg.getInstance();
            DefaultInstanceIdGenerator idGenerator =
                    new DefaultInstanceIdGenerator(instance.getServiceName(), instance.getIp(), instance.getPort());
            instance.setInstanceId(idGenerator.generateInstanceId());
        } catch (Exception e) {
            LOGGER.error("build meta msg error!", e);
        }
        return metaMsg;
    }

}
