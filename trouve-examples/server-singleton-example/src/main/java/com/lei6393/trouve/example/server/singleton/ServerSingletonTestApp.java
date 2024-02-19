package com.lei6393.trouve.example.server.singleton;

import com.lei6393.trouve.server.EnableTrouveDiscover;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

@SpringBootApplication
@EnableTrouveDiscover("openapi")
public class ServerSingletonTestApp {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ServerSingletonTestApp.class);
        // 设置默认端口
        application.setDefaultProperties(Collections.<String, Object>singletonMap("server.port", "8279"));
        application.run(args);
    }
}
