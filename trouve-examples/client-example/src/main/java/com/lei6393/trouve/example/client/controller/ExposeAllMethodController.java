package com.lei6393.trouve.example.client.controller;

import com.lei6393.trouve.client.api.ExposeApi;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/expose/all")
@ExposeApi
public class ExposeAllMethodController {

    @RequestMapping(value = "/{path}/one", produces = "application/json")
    @ResponseBody
    public String testMethod1() {
        return "{\"message\":\"success call client service\"}";
    }

    @RequestMapping(value = "/{path}/two", produces = "application/json")
    @ResponseBody
    public String testMethod2() {
        return "{\"message\":\"success call client service\"}";
    }
}
