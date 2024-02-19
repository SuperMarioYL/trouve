package com.lei6393.trouve.example.client.controller;

import com.lei6393.trouve.client.api.ExposeApi;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/expose/alone")
public class ExposeAloneMethodController {

    @RequestMapping(value = "/{path}/true", produces = "application/json")
    @ResponseBody
    @ExposeApi
    public String testMethodOne() {
        return "{\"message\":\"success call client service\"}";
    }

    @RequestMapping(value = "/{path}/false", produces = "application/json")
    @ResponseBody
    public String testMethodTwo() {
        return "{\"message\":\"success call client service\"}";
    }
}
