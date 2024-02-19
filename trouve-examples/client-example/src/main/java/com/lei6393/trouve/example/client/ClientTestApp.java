package com.lei6393.trouve.example.client;

import com.lei6393.trouve.client.EnableTrouveRegistry;
import com.lei6393.trouve.client.ServerAddress;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

@EnableTrouveRegistry(
        value = "test_service_name",  // servie name, 每个接入服务需要单独起名字
        serverAddresses = @ServerAddress(schema = "http", host = "127.0.0.1", port = 8279) // trouve 服务端地址，从配置优先获取，如果配置为空则使用注解
)
@SpringBootApplication
public class ClientTestApp {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ClientTestApp.class);
        // 设置默认端口
        application.setDefaultProperties(Collections.<String, Object>singletonMap("server.port", "8278"));
        application.run(args);
    }
}
