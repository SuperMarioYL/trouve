package com.lei6393.trouve.server.controller;

import com.lei6393.trouve.core.utils.GsonUtil;
import com.lei6393.trouve.server.consistency.AbstractHealthChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trouve/manager")
public class ManagerController {

    @Autowired
    AbstractHealthChecker healthChecker;


    @RequestMapping(value = "/health/instances", produces = "application/json")
    @ResponseBody
    public String getHealthInstances() {
        return GsonUtil.INSTANCE.toJson(healthChecker.getHealthInstanceIds());
    }

}
