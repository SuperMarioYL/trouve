package com.lei6393.trouve.server.controller;

import com.lei6393.trouve.core.data.instance.Instance;
import com.lei6393.trouve.server.instence.HttpRequestInstanceBuilder;
import com.lei6393.trouve.server.instence.InstanceOperatorRegistry;
import com.lei6393.trouve.server.instence.InstancePreCheckerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 实例 controller
 *
 * @author yulei
 * @date 2022/5/18 23:46
 */
@RestController
@RequestMapping("/trouve/instance")
@SuppressWarnings("unchecked")
public class InstanceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceController.class);

    @PostMapping()
    public ResponseEntity<String> register(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            Instance instance = HttpRequestInstanceBuilder.newBuilder().setRequest(request).build();
            if (InstancePreCheckerRegistry.preChecker(instance)) {
                InstanceOperatorRegistry.registerInstance(instance.getServiceName(), instance);
                LOGGER.debug("register instance {} success.", instance.getInstanceId());
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception e) {
            LOGGER.error("register instance error!", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping()
    public ResponseEntity<String> remove(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            Instance instance = HttpRequestInstanceBuilder.newBuilder().setRequest(request).build();
            if (InstancePreCheckerRegistry.preChecker(instance)) {
                InstanceOperatorRegistry.removeInstance(instance.getServiceName(), instance);
                LOGGER.debug("remove instance {} success.", instance.getInstanceId());
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception e) {
            LOGGER.error("remove instance error!", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping()
    public ResponseEntity<String> update(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            Instance instance = HttpRequestInstanceBuilder.newBuilder().setRequest(request).build();
            if (InstancePreCheckerRegistry.preChecker(instance)) {
                InstanceOperatorRegistry.updateInstance(instance.getServiceName(), instance);
                LOGGER.debug("update instance {} success.", instance.getInstanceId());
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception e) {
            LOGGER.error("update instance error!", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok().build();
    }

}
