package com.lei6393.trouve.example.server.nonsingleton;

import com.lei6393.trouve.server.EnableTrouveDiscover;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

@SpringBootApplication
@EnableTrouveDiscover("openapi")
public class ServerNonsingletonTestApp {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ServerNonsingletonTestApp.class);
        // 设置默认端口
        application.setDefaultProperties(Collections.<String, Object>singletonMap("server.port", "8279"));
        application.run(args);
    }
}
